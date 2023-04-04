package ca.aversa.insessionservice.util

import ca.aversa.insessionservice.model.Session
import ca.aversa.insessionservice.model.SessionAttendee
import ca.aversa.insessionservice.model.SessionAttendeeDetail
import ca.aversa.insessionservice.model.SessionAttendeeStatus
import ca.aversa.insessionservice.model.UserSession
import ca.aversa.insessionservice.model.entity.BookedSessionTableRow
import ca.aversa.insessionservice.model.request.CreateSessionRequest
import ca.aversa.insessionservice.model.request.UpdateSessionAttendee
import ca.aversa.insessionservice.util.TestDefaults.TEST_CLIENT_AUTH0_USER_ID
import ca.aversa.insessionservice.util.TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID
import ca.aversa.insessionservice.util.TestDefaults.TEST_ROOM_ID
import java.time.LocalDate
import java.time.ZonedDateTime

object SessionTestUtils {

    fun createSampleSession(
        clinicianId: String = TEST_CLINICIAN_AUTH0_USER_ID,
        attendees: List<SessionAttendee> = createSampleSessionAttendees(),
        startDate: ZonedDateTime = ZonedDateTime.now(),
        endDate: ZonedDateTime = ZonedDateTime.now(),
        subject: String = ""
    ): Session {
        return Session(
            clinicianId,
            attendees,
            startDate,
            endDate,
            subject
        )
    }

    fun createSampleSessionAttendees(): List<SessionAttendee> {
        return listOf(
            createSampleSessionAttendee(TEST_CLINICIAN_AUTH0_USER_ID),
            createSampleSessionAttendee(TEST_CLIENT_AUTH0_USER_ID)
        )
    }

    fun createSampleSessionAttendeeDetails(): List<SessionAttendeeDetail> {
        return listOf(
            SessionAttendeeDetail(TEST_CLINICIAN_AUTH0_USER_ID, "clinician"),
            SessionAttendeeDetail(TEST_CLIENT_AUTH0_USER_ID, "client"),
        )
    }

    fun createSampleSessionAttendee(
        userId: String,
        status: SessionAttendeeStatus = SessionAttendeeStatus.TENTATIVE,
        note: String? = "Attendee note"
    ): SessionAttendee {
        return SessionAttendee(userId, status, note)
    }

    fun createSampleBookedSessionTableRow(
        id: String = "sessionId",
        sortKey: String = TEST_CLINICIAN_AUTH0_USER_ID,
        sessionDate: LocalDate = LocalDate.now(),
        startDateTime: ZonedDateTime = ZonedDateTime.now(),
        endDateTime: ZonedDateTime = ZonedDateTime.now(),
        subject: String = "subject"
    ): BookedSessionTableRow {
        return BookedSessionTableRow(id, sortKey, sessionDate, startDateTime, endDateTime, subject)
    }

    fun createSampleCreateSessionRequest(
        clinicianId: String = TEST_CLINICIAN_AUTH0_USER_ID,
        clientIds: List<String> = listOf(TEST_CLIENT_AUTH0_USER_ID),
        startDate: ZonedDateTime = ZonedDateTime.now(),
        endDate: ZonedDateTime = ZonedDateTime.now(),
        subject: String = ""
    ): CreateSessionRequest {
        return CreateSessionRequest(
            clinicianId,
            clientIds,
            startDate,
            endDate,
            subject
        )
    }

    fun createSampleUpdateSessionAttendeeRequest(
        status: SessionAttendeeStatus = SessionAttendeeStatus.TENTATIVE,
        note: String? = "Attendee note"
    ): UpdateSessionAttendee {
        return UpdateSessionAttendee(status, note)
    }

    fun createSampleUserSession(
        clinicianId: String = TEST_CLINICIAN_AUTH0_USER_ID,
        attendees: List<SessionAttendee> = createSampleSessionAttendees(),
        sessionId: String = TEST_ROOM_ID,
        startDate: ZonedDateTime = ZonedDateTime.now(),
        endDate: ZonedDateTime = ZonedDateTime.now(),
        subject: String = "",
        isModerated: Boolean = false
    ): UserSession {
        return UserSession(
            clinicianId,
            attendees,
            sessionId,
            startDate,
            endDate,
            subject,
            isModerated
        )
    }
}