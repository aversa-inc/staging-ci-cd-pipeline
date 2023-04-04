package ca.aversa.insessionservice.controller

import ca.aversa.insessionservice.bo.SessionBo
import ca.aversa.insessionservice.context.RequestAttributeType
import ca.aversa.insessionservice.exception.SessionBoException
import ca.aversa.insessionservice.exception.SessionTimeConflictException
import ca.aversa.insessionservice.model.Session
import ca.aversa.insessionservice.model.SessionAttendee
import ca.aversa.insessionservice.model.SessionAttendeeDetail
import ca.aversa.insessionservice.util.SessionTestUtils
import ca.aversa.insessionservice.util.TestDefaults
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import javax.servlet.http.HttpServletRequest

internal class SessionControllerTest {

    private val context = TestDefaults.TEST_CLINICIAN_CONTEXT
    private val httpServletRequest: HttpServletRequest = mockk()
    private val sessionBo: SessionBo = mockk()
    private val controller = SessionController(sessionBo)

    @BeforeEach
    fun setup() {
        every {
            httpServletRequest.getAttribute(RequestAttributeType.CONTEXT.toString())
        }.returns(context)
    }

    @Test
    fun testCreateSessions_SuccessfullyCreateSession_ReturnOkResponse() {
        val request = SessionTestUtils.createSampleCreateSessionRequest()
        val session = Session.Mapper.from(request)

        justRun {
            sessionBo.createSessions(session, context.auth0UserId)
        }

        val response = controller.createSessions(httpServletRequest, request)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun testCreateSession_SessionBoThrowsTimeConflictException_ThrowSameException() {
        val request = SessionTestUtils.createSampleCreateSessionRequest()

        every {
            sessionBo.createSessions(any(), context.auth0UserId)
        }.throws(SessionTimeConflictException("Time conflict"))

        assertThrows<SessionTimeConflictException> {
            controller.createSessions(httpServletRequest, request)
        }
    }

    @Test
    fun testGetSessions_CallBoToRetrieveSessions_ReturnGetSessionResponse() {
        val expectedUserSessions = listOf(
            SessionTestUtils.createSampleUserSession()
        )

        every {
            sessionBo.getSessions(TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(expectedUserSessions)

        val response = controller.getSessions(httpServletRequest)

        assertNotNull(response)
        assertEquals(response.sessions, expectedUserSessions)
    }

    @Test
    fun testGetSessions_SessionBoThrowsSessionBoException_ThrowSameException() {
        every {
            sessionBo.getSessions(TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID)
        }.throws(SessionBoException("Something broke"))

        assertThrows<SessionBoException> {
            controller.getSessions(httpServletRequest)
        }
    }

    @Test
    fun testGetSession_CallSessionBoToGetSession_ReturnSessionInfo() {
        val expectedUserSession = SessionTestUtils.createSampleUserSession()

        every {
            sessionBo.getSession(TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID, TestDefaults.TEST_ROOM_ID)
        }.returns(expectedUserSession)

        val actualUserSession = controller.getSession(httpServletRequest, TestDefaults.TEST_ROOM_ID)

        assertEquals(expectedUserSession, actualUserSession)
    }

    @Test
    fun testUpdateSessionAttendee_CallSessionBoToUpdateAttendee_ReturnSuccessfulResponse() {
        val request = SessionTestUtils.createSampleUpdateSessionAttendeeRequest()
        val sessionAttendee = SessionAttendee.Mapper.from(context.auth0UserId, request.status, request.note)

        justRun {
            sessionBo.updateAttendee(TestDefaults.TEST_ROOM_ID, sessionAttendee)
        }

        val response = controller.updateSessionAttendee(httpServletRequest, TestDefaults.TEST_ROOM_ID, request)

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
    }

    @Test
    fun testGetSessionAttendeesDetails_CallSessionBoToGetDetails_ReturnSuccessfulResponse() {
        val expectedSessionAttendeeDetails = SessionTestUtils.createSampleSessionAttendeeDetails().map { attendee ->
            attendee.id to SessionAttendeeDetail(attendee.id, attendee.name)
        }.toMap()

        every {
            sessionBo.getAttendeeDetails(TestDefaults.TEST_ROOM_ID)
        }.returns(SessionTestUtils.createSampleSessionAttendeeDetails())

        val response = controller.getSessionAttendeesDetails(TestDefaults.TEST_ROOM_ID)

        assertEquals(expectedSessionAttendeeDetails, response.attendees)
    }
}