package ca.aversa.insessionservice.bo

import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.exception.PushNotificationServiceException
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.exception.SessionBoException
import ca.aversa.insessionservice.exception.SessionTimeConflictException
import ca.aversa.insessionservice.model.SessionAttendeeDetail
import ca.aversa.insessionservice.model.SessionAttendeeStatus
import ca.aversa.insessionservice.model.UserSession
import ca.aversa.insessionservice.model.entity.BookedSessionAssociationTableRow
import ca.aversa.insessionservice.model.entity.BookedSessionTableRow
import ca.aversa.insessionservice.model.entity.UserCommonTableRow
import ca.aversa.insessionservice.service.PushNotificationService
import ca.aversa.insessionservice.util.DateUtils
import ca.aversa.insessionservice.util.PushNotificationUtils
import ca.aversa.insessionservice.util.SessionTestUtils
import ca.aversa.insessionservice.util.TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID
import ca.aversa.insessionservice.util.TestDefaults.TEST_ROOM_ID
import ca.aversa.insessionservice.util.UserTestUtils
import ca.aversa.insessionservice.util.Utils
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.ZonedDateTime

internal class SessionBoTest {

    private val roomId = TEST_ROOM_ID
    private val sessionDateIndexName = "session_date_index"
    private val sessionDao: BasicDao<BookedSessionTableRow> = mockk()
    private val sessionAssociationDao: BasicDao<BookedSessionAssociationTableRow> = mockk()
    private val userCommonDao: BasicDao<UserCommonTableRow> = mockk()
    private val pushNotificationService: PushNotificationService = mockk()
    private val sessionBo = SessionBo(
        sessionDateIndexName, sessionDao, sessionAssociationDao, userCommonDao, pushNotificationService
    )
    private val attendees = listOf(
        SessionTestUtils.createSampleSessionAttendee("client1"),
        SessionTestUtils.createSampleSessionAttendee("client2"),
        SessionTestUtils.createSampleSessionAttendee(TEST_CLINICIAN_AUTH0_USER_ID),
    )
    private val session = SessionTestUtils.createSampleSession(
        attendees = attendees,
        startDate = ZonedDateTime.parse("2022-01-01T05:00:00Z"), endDate = ZonedDateTime.parse("2022-01-01T05:30:00Z")
    )
    private val userCommonTableRows = listOf(
        UserTestUtils.createSampleUserCommonTableRow(auth0UserId = "client1"),
        UserTestUtils.createSampleUserCommonTableRow(auth0UserId = "client2"),
        UserTestUtils.createSampleUserCommonTableRow(auth0UserId = TEST_CLINICIAN_AUTH0_USER_ID)
    )
    private val usersToNotify = setOf("client1", "client2")

    @BeforeEach
    fun setup() {
        mockkObject(Utils)

        every {
            Utils.createSecureId()
        }.returns(roomId)
    }

    @Test
    fun testCreateSession_NoConflictingSessionsFound_CreateNewSessions() {
        val sessionTableRow = session.toSessionEntity(roomId, TEST_CLINICIAN_AUTH0_USER_ID)
        val sessionAssociationRows = session.toSessionAssociationEntities(roomId)
        val nonConflictingSessions = createNonConflictingSessions()

        every {
            sessionDao.queryGsi(sessionDateIndexName, any(), any())
        }.returns(nonConflictingSessions)

        every {
            userCommonDao.batchLoad(usersToNotify)
        }.returns(userCommonTableRows)

        justRun {
            sessionDao.update(sessionTableRow)
        }

        justRun {
            sessionAssociationDao.batchSave(sessionAssociationRows)
        }

        justRun {
            pushNotificationService.sendNotification(any(), any(), any())
        }

        sessionBo.createSessions(session, session.clinicianId)

        verify {
            pushNotificationService.sendNotification(
                userCommonTableRows[0].gcmConfigs[0].gcmDeviceSnsEndpoint,
                session.subject,
                PushNotificationUtils.getNewSessionCreationBody(DateUtils.createUserReadableDate(session.startDateTime))
            )
            pushNotificationService.sendNotification(
                userCommonTableRows[1].gcmConfigs[0].gcmDeviceSnsEndpoint,
                session.subject,
                PushNotificationUtils.getNewSessionCreationBody(DateUtils.createUserReadableDate(session.startDateTime))
            )
        }
    }

    @Test
    fun testCreateSession_NoBookedSessionsFound_CreateNewSessions() {
        val sessionTableRow = session.toSessionEntity(roomId, TEST_CLINICIAN_AUTH0_USER_ID)
        val sessionAssociationRows = session.toSessionAssociationEntities(roomId)

        every {
            sessionDao.queryGsi(sessionDateIndexName, any(), any())
        }.returns(emptyList())

        every {
            userCommonDao.batchLoad(usersToNotify)
        }.returns(userCommonTableRows)

        justRun {
            sessionDao.update(sessionTableRow)
        }

        justRun {
            sessionAssociationDao.batchSave(sessionAssociationRows)
        }

        justRun {
            pushNotificationService.sendNotification(any(), any(), any())
        }

        sessionBo.createSessions(session, session.clinicianId)

        verify {
            pushNotificationService.sendNotification(
                userCommonTableRows[0].gcmConfigs[0].gcmDeviceSnsEndpoint,
                session.subject,
                PushNotificationUtils.getNewSessionCreationBody(DateUtils.createUserReadableDate(session.startDateTime))
            )
            pushNotificationService.sendNotification(
                userCommonTableRows[1].gcmConfigs[0].gcmDeviceSnsEndpoint,
                session.subject,
                PushNotificationUtils.getNewSessionCreationBody(DateUtils.createUserReadableDate(session.startDateTime))
            )
        }
    }

    @Test
    fun testCreateSession_ConflictingSessionsFound_ThrowConflictingSessionFoundException() {
        val conflictingSessions = listOf(
            createConflictingSession("2022-01-01T05:00:00Z", "2022-01-01T05:30:00Z"),
            createConflictingSession("2022-01-01T05:15:00Z", "2022-01-01T05:25:00Z"),
            createConflictingSession("2022-01-01T04:30:00Z", "2022-01-01T05:30:00Z"),
            createConflictingSession("2022-01-01T04:30:00Z", "2022-01-01T05:15:00Z")
        )

        for (conflictingSession in conflictingSessions) {
            every {
                sessionDao.queryGsi(sessionDateIndexName, any(), any())
            }.returns(listOf(conflictingSession))

            assertThrows<SessionTimeConflictException> {
                sessionBo.createSessions(session, session.clinicianId)
            }
        }
    }

    @Test
    fun testCreateSession_DaoThrowsExceptionWhenQueryingForConflicts_ThrowSessionBoException() {
        every {
            sessionDao.queryGsi(sessionDateIndexName, any(), any())
        }.throws(RuntimeException("Something broke"))

        assertThrows<SessionBoException> {
            sessionBo.createSessions(session, session.clinicianId)
        }
    }

    @Test
    fun testCreateSession_DaoThrowsExceptionWhenSavingInSessionsTable_ThrowSessionBoException() {
        val sessionTableRow = session.toSessionEntity(roomId, TEST_CLINICIAN_AUTH0_USER_ID)
        val nonConflictingSessions = createNonConflictingSessions()

        every {
            sessionDao.queryGsi(sessionDateIndexName, any(), any())
        }.returns(nonConflictingSessions)

        every {
            sessionDao.update(sessionTableRow)
        }.throws(RuntimeException("Something broke"))

        assertThrows<SessionBoException> {
            sessionBo.createSessions(session, session.clinicianId)
        }
    }

    @Test
    fun testCreateSession_DaoThrowsExceptionWhenSavingInSessionsAssociationTable_ThrowSessionBoException() {
        val sessionTableRow = session.toSessionEntity(roomId, TEST_CLINICIAN_AUTH0_USER_ID)
        val sessionAssociationRows = session.toSessionAssociationEntities(roomId)
        val nonConflictingSessions = createNonConflictingSessions()

        every {
            sessionDao.queryGsi(sessionDateIndexName, any(), any())
        }.returns(nonConflictingSessions)

        every {
            sessionAssociationDao.batchSave(sessionAssociationRows)
        }.throws(RuntimeException("Something broke"))

        justRun {
            sessionDao.update(sessionTableRow)
        }

        assertThrows<SessionBoException> {
            sessionBo.createSessions(session, session.clinicianId)
        }
    }

    @Test
    fun testCreateSession_UserCommonDaoThrowsException_ThrowSessionBoException() {
        val sessionTableRow = session.toSessionEntity(roomId, TEST_CLINICIAN_AUTH0_USER_ID)
        val sessionAssociationRows = session.toSessionAssociationEntities(roomId)
        val nonConflictingSessions = createNonConflictingSessions()

        every {
            sessionDao.queryGsi(sessionDateIndexName, any(), any())
        }.returns(nonConflictingSessions)

        every {
            sessionAssociationDao.batchSave(sessionAssociationRows)
        }.throws(RuntimeException("Something broke"))

        every {
            userCommonDao.batchLoad(usersToNotify)
        }.throws(RuntimeException("something broke"))

        justRun {
            sessionDao.update(sessionTableRow)
        }

        assertThrows<SessionBoException> {
            sessionBo.createSessions(session, session.clinicianId)
        }
    }

    @Test
    fun testCreateSession_PushNotificationServiceThrowsException_ThrowSessionBoException() {
        val sessionTableRow = session.toSessionEntity(roomId, TEST_CLINICIAN_AUTH0_USER_ID)
        val sessionAssociationRows = session.toSessionAssociationEntities(roomId)
        val nonConflictingSessions = createNonConflictingSessions()

        every {
            sessionDao.queryGsi(sessionDateIndexName, any(), any())
        }.returns(nonConflictingSessions)

        every {
            sessionAssociationDao.batchSave(sessionAssociationRows)
        }.throws(RuntimeException("Something broke"))

        every {
            userCommonDao.batchLoad(usersToNotify)
        }.returns(userCommonTableRows)

        every {
            pushNotificationService.sendNotification(any(), any(), any())
        }.throws(PushNotificationServiceException("Something brokeee"))

        justRun {
            sessionDao.update(sessionTableRow)
        }

        assertThrows<SessionBoException> {
            sessionBo.createSessions(session, session.clinicianId)
        }
    }

    @Test
    fun testGetSessions_ClinicianHasUpcomingSessions_ReturnSessions() {
        val sessionTableRow = session.toSessionEntity(roomId, TEST_CLINICIAN_AUTH0_USER_ID)
        val sessionAssociationRows = session.toSessionAssociationEntities(roomId)
        val sessionIds = sessionAssociationRows.map(BookedSessionAssociationTableRow::sortKey)
        val expectedUserSessions = listOf(
            UserSession(
                session.clinicianId,
                session.attendees,
                roomId,
                session.startDateTime,
                session.endDateTime,
                session.subject,
                session.isModerated
            )
        )

        every {
            sessionAssociationDao.query(any(), any())
        }.returns(sessionAssociationRows)

        every {
            sessionDao.batchLoad(sessionIds)
        }.returns(listOf(sessionTableRow))

        val actualUserSessions = sessionBo.getSessions(session.clinicianId)

        assertEquals(expectedUserSessions, actualUserSessions)
    }

    @Test
    fun testGetSessions_ClientHasUpcomingSessions_ReturnSessions() {
        val sessionTableRow = session.toSessionEntity(roomId, TEST_CLINICIAN_AUTH0_USER_ID)
        val sessionAssociationRows = session.toSessionAssociationEntities(roomId)
        val sessionIds = sessionAssociationRows.map(BookedSessionAssociationTableRow::sortKey)
        val expectedUserSessions = listOf(
            UserSession(
                session.clinicianId,
                session.attendees,
                roomId,
                session.startDateTime,
                session.endDateTime,
                session.subject,
                session.isModerated
            )
        )

        every {
            sessionAssociationDao.query(any(), any())
        }.returns(sessionAssociationRows)

        every {
            sessionDao.batchLoad(sessionIds)
        }.returns(listOf(sessionTableRow))

        val actualUserSessions = sessionBo.getSessions(session.attendees[0].userId)

        assertEquals(expectedUserSessions, actualUserSessions)
    }

    @Test
    fun testGetSessions_AssociationsDaoThrowsException_ThrowSessionBoException() {
        every {
            sessionAssociationDao.query(any(), any())
        }.throws(RuntimeException("Something broke"))

        assertThrows<SessionBoException> {
            sessionBo.getSessions(session.attendees[0].userId)
        }
    }

    @Test
    fun testGetSessions_SessionsDaoThrowsException_ThrowSessionBoException() {
        val sessionAssociationRows = session.toSessionAssociationEntities(roomId)
        val sessionIds = sessionAssociationRows.map(BookedSessionAssociationTableRow::sortKey)

        every {
            sessionAssociationDao.query(any(), any())
        }.returns(sessionAssociationRows)

        every {
            sessionDao.batchLoad(sessionIds)
        }.throws(RuntimeException("Something broke"))

        assertThrows<SessionBoException> {
            sessionBo.getSessions(session.attendees[0].userId)
        }
    }

    @Test
    fun testGetSession_SessionFoundForUser_ReturnUserSession() {
        val sessionAssociationRows = session.toSessionAssociationEntities(roomId)
        val bookedSessionTableRow = session.toSessionEntity(roomId, TEST_CLINICIAN_AUTH0_USER_ID)
        val expectedUserSession = UserSession(
            session.clinicianId,
            session.attendees,
            roomId,
            session.startDateTime,
            session.endDateTime,
            session.subject,
            session.isModerated
        )

        every {
            sessionAssociationDao.get(session.attendees[0].userId, roomId)
        }.returns(sessionAssociationRows[0])

        every {
            sessionDao.get(roomId)
        }.returns(bookedSessionTableRow)

        val actualUserSession = sessionBo.getSession(session.attendees[0].userId, roomId)

        assertEquals(expectedUserSession, actualUserSession)
    }

    @Test
    fun testGetSession_SessionNotFoundForUser_ThrowResourceNotFoundException() {
        every {
            sessionAssociationDao.get(session.attendees[0].userId, roomId)
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            sessionBo.getSession(session.attendees[0].userId, roomId)
        }
    }

    @Test
    fun testGetSession_SessionForUserFoundButSessionNotFoundEdgeCase_ThrowResourceNotFoundException() {
        val sessionAssociationRows = session.toSessionAssociationEntities(roomId)

        every {
            sessionAssociationDao.get(session.attendees[0].userId, roomId)
        }.returns(sessionAssociationRows[0])

        every {
            sessionDao.get(roomId)
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            sessionBo.getSession(session.attendees[0].userId, roomId)
        }
    }

    @Test
    fun testUpdateSessionAttendee_SessionFoundAndAttendeeUpdatedByRequester_Successful() {
        val userId = TEST_CLINICIAN_AUTH0_USER_ID
        val sessionAssociationRows = session.toSessionAssociationEntities(roomId)
        val bookedSessionTableRow = session.toSessionEntity(roomId, TEST_CLINICIAN_AUTH0_USER_ID)
        val attendeeToUpdate = SessionTestUtils.createSampleSessionAttendee(
            userId, SessionAttendeeStatus.ACCEPTED, note = "Accepted"
        )

        every {
            sessionAssociationDao.get(userId, roomId)
        }.returns(sessionAssociationRows[0])

        every {
            sessionDao.get(roomId)
        }.returns(bookedSessionTableRow)

        every {
            userCommonDao.batchLoad(usersToNotify)
        }.returns(userCommonTableRows)

        every {
            userCommonDao.batchLoad(listOf(userId))
        }.returns(listOf(userCommonTableRows[0]))

        bookedSessionTableRow.attendees[userId] = attendeeToUpdate.toEntity()

        justRun {
            sessionDao.update(bookedSessionTableRow)
        }

        justRun {
            pushNotificationService.sendNotification(any(), any(), any())
        }

        sessionBo.updateAttendee(roomId, attendeeToUpdate)

        verify {
            pushNotificationService.sendNotification(
                userCommonTableRows[0].gcmConfigs[0].gcmDeviceSnsEndpoint,
                PushNotificationUtils.getUpdatedAttendeeTitle(
                    userCommonTableRows[0].userFriendlyIdentifier, attendeeToUpdate.status
                ),
                attendeeToUpdate.note!!
            )
            pushNotificationService.sendNotification(
                userCommonTableRows[1].gcmConfigs[0].gcmDeviceSnsEndpoint,
                PushNotificationUtils.getUpdatedAttendeeTitle(
                    userCommonTableRows[0].userFriendlyIdentifier, attendeeToUpdate.status
                ),
                attendeeToUpdate.note!!
            )
        }
    }

    @Test
    fun testUpdateSessionAttendee_SessionFoundAndAttendeeUpdatedByNonRequester_Successful() {
        val userId = session.attendees[0].userId
        val sessionAssociationRows = session.toSessionAssociationEntities(roomId)
        val bookedSessionTableRow = session.toSessionEntity(roomId, TEST_CLINICIAN_AUTH0_USER_ID)
        val attendeeToUpdate = SessionTestUtils.createSampleSessionAttendee(
            userId, SessionAttendeeStatus.ACCEPTED, note = "Accepted"
        )

        every {
            sessionAssociationDao.get(userId, roomId)
        }.returns(sessionAssociationRows[0])

        every {
            sessionDao.get(roomId)
        }.returns(bookedSessionTableRow)

        every {
            userCommonDao.batchLoad(setOf(TEST_CLINICIAN_AUTH0_USER_ID))
        }.returns(listOf(userCommonTableRows[2]))

        every {
            userCommonDao.batchLoad(listOf(userId))
        }.returns(listOf(userCommonTableRows[0]))

        bookedSessionTableRow.attendees[userId] = attendeeToUpdate.toEntity()

        justRun {
            sessionDao.update(bookedSessionTableRow)
        }

        justRun {
            pushNotificationService.sendNotification(any(), any(), any())
        }

        sessionBo.updateAttendee(roomId, attendeeToUpdate)

        verify {
            pushNotificationService.sendNotification(
                userCommonTableRows[2].gcmConfigs[0].gcmDeviceSnsEndpoint,
                PushNotificationUtils.getUpdatedAttendeeTitle(
                    userCommonTableRows[0].userFriendlyIdentifier, attendeeToUpdate.status
                ),
                attendeeToUpdate.note!!
            )
        }
    }

    @Test
    fun testUpdateSessionAttendee_SessionNotFoundForUser_ThrowResourceNotFoundException() {
        val userId = session.attendees[0].userId
        val attendeeToUpdate = SessionTestUtils.createSampleSessionAttendee(
            userId, SessionAttendeeStatus.ACCEPTED, note = "Accepted"
        )

        every {
            sessionAssociationDao.get(userId, roomId)
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            sessionBo.updateAttendee(roomId, attendeeToUpdate)
        }
    }

    @Test
    fun testUpdateSessionAttendee_SessionForUserFoundButSessionNotFoundEdgeCase_ThrowResourceNotFoundException() {
        val userId = session.attendees[0].userId
        val sessionAssociationRows = session.toSessionAssociationEntities(roomId)
        val attendeeToUpdate = SessionTestUtils.createSampleSessionAttendee(
            userId, SessionAttendeeStatus.ACCEPTED, note = "Accepted"
        )

        every {
            sessionAssociationDao.get(userId, roomId)
        }.returns(sessionAssociationRows[0])

        every {
            sessionDao.get(roomId)
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            sessionBo.updateAttendee(roomId, attendeeToUpdate)
        }
    }

    @Test
    fun testGetSessionAttendeeDetails_SessionExists_ReturnAttendeeCommonConfigDetails() {
        val bookedSessionTableRow = session.toSessionEntity(roomId, TEST_CLINICIAN_AUTH0_USER_ID)
        val expectedAttendeeDetails = userCommonTableRows.map { config ->
            SessionAttendeeDetail(config.id, config.userFriendlyIdentifier)
        }

        every {
            sessionDao.get(roomId)
        }.returns(bookedSessionTableRow)

        every {
            userCommonDao.batchLoad(bookedSessionTableRow.attendees.keys)
        }.returns(userCommonTableRows)

        val actualAttendeeDetails = sessionBo.getAttendeeDetails(roomId)

        assertEquals(expectedAttendeeDetails, actualAttendeeDetails)
    }

    @Test
    fun testUpdateSessionAttendee_DeclineUpdateDoneByRequester_DeleteSessionAssociationRows() {
        val userId = TEST_CLINICIAN_AUTH0_USER_ID
        val sessionAssociationRows = session.toSessionAssociationEntities(roomId)
        val bookedSessionTableRow = session.toSessionEntity(roomId, TEST_CLINICIAN_AUTH0_USER_ID)
        val attendeeToUpdate = SessionTestUtils.createSampleSessionAttendee(
            userId, SessionAttendeeStatus.DECLINED, note = "Cancelled"
        )

        every {
            sessionAssociationDao.get(userId, roomId)
        }.returns(sessionAssociationRows[0])

        every {
            sessionDao.get(roomId)
        }.returns(bookedSessionTableRow)

        every {
            userCommonDao.batchLoad(usersToNotify)
        }.returns(userCommonTableRows)

        every {
            userCommonDao.batchLoad(listOf(userId))
        }.returns(listOf(userCommonTableRows[0]))

        bookedSessionTableRow.attendees[userId] = attendeeToUpdate.toEntity()

        justRun {
            sessionDao.update(bookedSessionTableRow)
        }

        justRun {
            pushNotificationService.sendNotification(any(), any(), any())
        }

        justRun {
            sessionAssociationDao.batchDelete(any())
        }

        justRun {
            sessionDao.delete(bookedSessionTableRow)
        }

        sessionBo.updateAttendee(roomId, attendeeToUpdate)

        verify {
            pushNotificationService.sendNotification(
                userCommonTableRows[0].gcmConfigs[0].gcmDeviceSnsEndpoint,
                PushNotificationUtils.getUpdatedAttendeeTitle(
                    userCommonTableRows[0].userFriendlyIdentifier, attendeeToUpdate.status
                ),
                attendeeToUpdate.note!!
            )
            pushNotificationService.sendNotification(
                userCommonTableRows[1].gcmConfigs[0].gcmDeviceSnsEndpoint,
                PushNotificationUtils.getUpdatedAttendeeTitle(
                    userCommonTableRows[0].userFriendlyIdentifier, attendeeToUpdate.status
                ),
                attendeeToUpdate.note!!
            )
            sessionDao.delete(bookedSessionTableRow)
        }
    }

    private fun createNonConflictingSessions(): List<BookedSessionTableRow> {
        return listOf(
            SessionTestUtils.createSampleBookedSessionTableRow(
                sessionDate = LocalDate.parse("2022-01-01"),
                startDateTime = ZonedDateTime.parse("2022-01-01T04:30:00Z"),
                endDateTime = ZonedDateTime.parse("2022-01-01T05:00:00Z")
            ),
            SessionTestUtils.createSampleBookedSessionTableRow(
                sessionDate = LocalDate.parse("2022-01-01"),
                startDateTime = ZonedDateTime.parse("2022-01-01T05:30:00Z"),
                endDateTime = ZonedDateTime.parse("2022-01-01T06:00:00Z")
            )
        )
    }

    private fun createConflictingSession(startDateTime: String, endDateTime: String): BookedSessionTableRow {
        return SessionTestUtils.createSampleBookedSessionTableRow(
            sessionDate = LocalDate.parse("2022-01-01"),
            startDateTime = ZonedDateTime.parse(startDateTime),
            endDateTime = ZonedDateTime.parse(endDateTime)
        )
    }
}
