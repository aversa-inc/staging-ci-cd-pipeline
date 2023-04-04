package ca.aversa.insessionservice.model

import ca.aversa.insessionservice.model.entity.BookedSessionAssociationTableRow
import ca.aversa.insessionservice.model.entity.BookedSessionTableRow
import ca.aversa.insessionservice.model.entity.SessionAttendeeDocument
import ca.aversa.insessionservice.model.request.CreateSessionRequest
import java.time.ZonedDateTime

data class Session(

    val clinicianId: String,
    val attendees: List<SessionAttendee>,
    val startDateTime: ZonedDateTime,
    val endDateTime: ZonedDateTime,
    val subject: String,
    val isModerated: Boolean = false
) {
    fun toSessionEntity(sessionId: String, requesterUserId: String): BookedSessionTableRow {
        val attendeeDocuments = attendees.map(SessionAttendee::toEntity)
            .map { attendeeDocument ->
                attendeeDocument.userId to attendeeDocument
            }
            .toMap() as MutableMap<String, SessionAttendeeDocument>

        return BookedSessionTableRow(
            sessionId,
            clinicianId,
            startDateTime.toLocalDate(),
            startDateTime,
            endDateTime,
            subject,
            attendeeDocuments,
            requesterUserId,
            isModerated
        )
    }

    fun toSessionAssociationEntities(sessionId: String): List<BookedSessionAssociationTableRow> {
        val ttl = endDateTime.plusWeeks(1).toInstant().epochSecond

        return attendees.map { attendee ->
            BookedSessionAssociationTableRow(
                attendee.userId,
                sessionId,
                ttl,
                startDateTime,
                endDateTime
            )
        }
    }

    object Mapper {

        fun from(request: CreateSessionRequest): Session {
            val userIds = request.clientIds + request.clinicianId
            val attendees = userIds.map(SessionAttendee.Mapper::from)

            return Session(
                request.clinicianId,
                attendees,
                request.startDateTime,
                request.endDateTime,
                request.subject,
                request.isModerated
            )
        }
    }
}