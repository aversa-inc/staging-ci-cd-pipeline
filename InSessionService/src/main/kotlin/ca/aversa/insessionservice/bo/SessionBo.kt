package ca.aversa.insessionservice.bo

import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.exception.SessionBoException
import ca.aversa.insessionservice.exception.SessionTimeConflictException
import ca.aversa.insessionservice.model.Session
import ca.aversa.insessionservice.model.SessionAttendee
import ca.aversa.insessionservice.model.SessionAttendeeDetail
import ca.aversa.insessionservice.model.SessionAttendeeStatus
import ca.aversa.insessionservice.model.UserSession
import ca.aversa.insessionservice.model.entity.BookedSessionAssociationTableRow
import ca.aversa.insessionservice.model.entity.BookedSessionTableRow
import ca.aversa.insessionservice.model.entity.UserCommonTableRow
import ca.aversa.insessionservice.service.PushNotificationService
import ca.aversa.insessionservice.util.DateUtils
import ca.aversa.insessionservice.util.PushNotificationUtils
import ca.aversa.insessionservice.util.Utils
import com.amazonaws.services.dynamodbv2.model.AttributeValue

class SessionBo(

    private val sessionDateIndexName: String,
    private val sessionDao: BasicDao<BookedSessionTableRow>,
    private val sessionAssociationDao: BasicDao<BookedSessionAssociationTableRow>,
    private val userCommonDao: BasicDao<UserCommonTableRow>,
    private val pushNotificationService: PushNotificationService
) {

    fun createSessions(session: Session, requesterUserId: String) {
        try {
            if(doesSessionTimeConflictExist(session)) {
                throw SessionTimeConflictException("Clinician is already booked for the specified session time: $session")
            }

            // Create the session id (room id), and create session entity rows
            val roomId = Utils.createSecureId()
            val sessionTableRows = session.toSessionEntity(roomId, requesterUserId)
            sessionTableRows.attendees[requesterUserId]!!.status = SessionAttendeeStatus.ACCEPTED

            // Create user session association rows for all attendees (clients and clinicians)
            val sessionAssociationTableRows = session.toSessionAssociationEntities(roomId)

            // Update the databases with the sessions
            sessionDao.update(sessionTableRows)
            sessionAssociationDao.batchSave(sessionAssociationTableRows)

            // Send notifications to users excluding the one who created the sessions
            val userIds = session.attendees.map { it.userId }
            val usersToNotify = HashSet<String>(userIds)
            usersToNotify.remove(requesterUserId)

            val formattedDateTime = DateUtils.createUserReadableDate(session.startDateTime)
            sendPushNotificationToUsers(usersToNotify) { user ->
                val title = session.subject
                val body = PushNotificationUtils.getNewSessionCreationBody(formattedDateTime)

                Pair(title, body)
            }
        }
        catch (e: Exception) {
            when(e) {
                is SessionTimeConflictException -> throw e
                else -> throw SessionBoException(cause = e)
            }
        }
    }

    /**
     * Query the sessions associations table to get all session Ids, and then query the sessions table to get their details.
     */
    fun getSessions(userId: String): List<UserSession> {
        try {
            val userSessionRows = getUserSessionAssociationRows(userId)
            val sessionIds = userSessionRows.map(BookedSessionAssociationTableRow::sortKey)

            return sessionDao.batchLoad(sessionIds).map(this::convertRowToUserSession)
        }
        catch (e: Exception) {
            throw SessionBoException(cause = e)
        }
    }

    fun getSession(userId: String, sessionId: String): UserSession {
        try {
            val row = getSessionTableRowForUser(userId, sessionId)

            return convertRowToUserSession(row)
        }
        catch (e: Exception) {
            when(e) {
                is ResourceNotFoundException -> throw e
                else -> throw SessionBoException(cause = e)
            }
        }
    }

    fun updateAttendee(sessionId: String, updatedAttendee: SessionAttendee) {
        try {
            val userId = updatedAttendee.userId
            val row = getSessionTableRowForUser(userId, sessionId)
            row.attendees[userId]
                ?: throw ResourceNotFoundException("User $userId not found amongst session attendees ${row.attendees}")

            row.attendees[userId] = updatedAttendee.toEntity()

            sessionDao.update(row)

            val updatedAttendeeDetail = getUserCommonConfig(listOf(userId))[0]

            // If update is being made by requester, send notification to all attendees except requester
            // else, send notification to requester
            val attendeesToNotify: Set<String> = if(updatedAttendee.userId == row.requesterUserId) {
                val usersToNotify = row.attendees
                usersToNotify.remove(row.requesterUserId)

                usersToNotify.keys
            }
            else {
                setOf(row.requesterUserId)
            }

            sendPushNotificationToUsers(attendeesToNotify) {
                val title = PushNotificationUtils.getUpdatedAttendeeTitle(
                    updatedAttendeeDetail.userFriendlyIdentifier, updatedAttendee.status
                )
                val body = updatedAttendee.note ?: "No reason given."

                Pair(title, body)
            }

            // Cancel session if requester has declined
            if(updatedAttendee.status == SessionAttendeeStatus.DECLINED && updatedAttendee.userId == row.requesterUserId) {
                cancelSession(sessionId)
            }
        }
        catch (e: Exception) {
            when(e) {
                is ResourceNotFoundException -> throw e
                else -> throw SessionBoException(cause = e)
            }
        }
    }

    fun getAttendeeDetails(sessionId: String): List<SessionAttendeeDetail> {
        val session = sessionDao.get(sessionId)
            ?: throw ResourceNotFoundException("Session $sessionId not found")

        val userCommonConfigs = userCommonDao.batchLoad(session.attendees.keys)

        return userCommonConfigs.map { config ->
            SessionAttendeeDetail(config.id, config.userFriendlyIdentifier)
        }
    }

    private fun getSessionTableRowForUser(userId: String, sessionId: String): BookedSessionTableRow {
        sessionAssociationDao.get(userId, sessionId)
            ?: throw ResourceNotFoundException("Session $sessionId not found for user $userId")

        return sessionDao.get(sessionId)
            ?: throw ResourceNotFoundException("Session $sessionId not found")
    }

    private fun convertRowToUserSession(row: BookedSessionTableRow): UserSession {
        val attendees = row.attendees.values.map(SessionAttendee.Mapper::from).toList()

        return UserSession(
            row.clinicianId,
            attendees,
            row.id,
            row.startDateTime,
            row.endDateTime,
            row.subject,
            row.isModerated
        )
    }

    private fun sendPushNotificationToUsers(userIds: Collection<String>,
                                            titleBodyProvider: (user: UserCommonTableRow) -> Pair<String, String>) {
        val users = getUserCommonConfig(userIds)
        users.forEach { user ->
            val titleAndBody = titleBodyProvider(user)

            user.gcmConfigs.forEach { gcmConfig ->
                pushNotificationService.sendNotification(gcmConfig.gcmDeviceSnsEndpoint, titleAndBody.first, titleAndBody.second)
            }
        }
    }

    private fun getUserCommonConfig(userIds: Collection<String>): List<UserCommonTableRow> {
        return userCommonDao.batchLoad(userIds)
    }

    private fun getUserSessionAssociationRows(userId: String): List<BookedSessionAssociationTableRow> {
        val keyConditionExpression = "id = :id"
        val valuesMap = mapOf(
            ":id" to AttributeValue().withS(userId)
        )

        return sessionAssociationDao.query(keyConditionExpression, valuesMap)
    }

    private fun doesSessionTimeConflictExist(session: Session): Boolean {
        val keyConditionExpression = "session_date = :session_date and clinician = :clinician"
        val valuesMap = mutableMapOf<String, AttributeValue>(
            ":session_date" to AttributeValue().withS(session.startDateTime.toLocalDate().toString()),
            ":clinician" to AttributeValue().withS(session.clinicianId)
        )

        val rows = sessionDao.queryGsi(sessionDateIndexName, keyConditionExpression, valuesMap)

        return rows.filter { row ->
            session.startDateTime.isBefore(row.endDateTime) && session.endDateTime.isAfter(row.startDateTime)
        }.any()
    }

    private fun cancelSession(sessionId: String) {
        val sessionRow = sessionDao.get(sessionId)
            ?: throw ResourceNotFoundException("Session $sessionId not found while cancelling")

        val sessionUserRows = sessionRow.attendees.keys.map { id ->
            BookedSessionAssociationTableRow(id, sessionId)
        }

        println("Cancelling session: $sessionId")

        sessionDao.delete(sessionRow)
        sessionAssociationDao.batchDelete(sessionUserRows)
    }
}
