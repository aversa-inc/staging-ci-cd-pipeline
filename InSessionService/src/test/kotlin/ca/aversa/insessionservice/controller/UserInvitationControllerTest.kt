package ca.aversa.insessionservice.controller

import ca.aversa.insessionservice.bo.UserInvitationBo
import ca.aversa.insessionservice.context.RequestAttributeType
import ca.aversa.insessionservice.exception.UserInvitationBoException
import ca.aversa.insessionservice.util.Constants
import ca.aversa.insessionservice.util.RegisterAndLoginTestUtils.createSampleRegisterClientRequest
import ca.aversa.insessionservice.util.RegisterAndLoginTestUtils.createSampleRegisterClinicianRequest
import ca.aversa.insessionservice.util.RegisterAndLoginTestUtils.createSampleUserInvitationApproveRequest
import ca.aversa.insessionservice.util.RegisterAndLoginTestUtils.createSampleUserInvitationRejectRequest
import ca.aversa.insessionservice.util.TestDefaults
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import javax.servlet.http.HttpServletRequest

internal class UserInvitationControllerTest {

    private val httpServletRequest: HttpServletRequest = mockk()
    private val invitationBo: UserInvitationBo = mockk()
    private val invitationController: UserInvitationController = UserInvitationController(invitationBo)

    @Test
    fun testInviteClinician_CallInvitationBoToInviteClinician_ReturnSuccessfulResponse() {
        val clinicianRequest = createSampleRegisterClinicianRequest()

        every {
            httpServletRequest.getAttribute(RequestAttributeType.CONTEXT.toString())
        }.returns(TestDefaults.TEST_CLINICIAN_CONTEXT)

        justRun {
            invitationBo.invite(any(), any())
        }

        val response = invitationController.inviteClinician(clinicianRequest, httpServletRequest)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun testInviteClinician_InvitationBoThrowsException_ThrowException() {
        val clinicianRequest = createSampleRegisterClinicianRequest()

        every {
            httpServletRequest.getAttribute(RequestAttributeType.CONTEXT.toString())
        }.returns(TestDefaults.TEST_CLINICIAN_CONTEXT)

        every {
            invitationBo.invite(any(), any())
        }.throws(UserInvitationBoException("something went wrong"))

        assertThrows<UserInvitationBoException> {
            invitationController.inviteClinician(clinicianRequest, httpServletRequest)
        }
    }

    @Test
    fun testInviteClient_CallInvitationBoToInviteClient_ReturnSuccessfulResponse() {
        val clientRequest = createSampleRegisterClientRequest()

        every {
            httpServletRequest.getAttribute(RequestAttributeType.CONTEXT.toString())
        }.returns(TestDefaults.TEST_CLIENT_CONTEXT)

        justRun {
            invitationBo.invite(any(), any())
        }

        val response = invitationController.inviteClient(clientRequest, httpServletRequest)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun testInviteClient_InvitationBoThrowsException_ThrowException() {
        val clientRequest = createSampleRegisterClientRequest()

        every {
            httpServletRequest.getAttribute(RequestAttributeType.CONTEXT.toString())
        }.returns(TestDefaults.TEST_CLIENT_CONTEXT)

        every {
            invitationBo.invite(any(), any())
        }.throws(UserInvitationBoException("something went wrong"))

        assertThrows<UserInvitationBoException> {
            invitationController.inviteClient(clientRequest, httpServletRequest)
        }
    }

    @Test
    fun testApproveInvitation_CallInvitationBoToApprove_ReturnSuccessfulResponse() {
        val invitationRequest = createSampleUserInvitationApproveRequest()

        justRun {
            invitationBo.approve(invitationRequest.id)
        }

        val response = invitationController.approveInvitation(invitationRequest)

        assertNotNull(response)
        assertEquals("${Constants.INVITATION_ACCEPTED_STATIC_LINK}?type=${invitationRequest.type ?: ""}", response.url)
    }

    @Test
    fun testApproveInvitation_InvitationBoThrowsException_ThrowSameException() {
        val invitationRequest = createSampleUserInvitationApproveRequest()

        every {
            invitationBo.approve(invitationRequest.id)
        }.throws(Exception("something broke"))

        assertThrows<Exception> {
            invitationController.approveInvitation(invitationRequest)
        }
    }

    @Test
    fun testRejectInvitation_CallInvitationBoToReject_ReturnSuccessfulResponse() {
        val invitationRequest = createSampleUserInvitationRejectRequest()

        justRun {
            invitationBo.reject(invitationRequest.id)
        }

        val response = invitationController.rejectInvitation(invitationRequest)

        assertNotNull(response)
        assertEquals(Constants.INVITATION_REJECTED_STATIC_LINK, response.url)
    }

    @Test
    fun testRejectInvitation_InvitationBoThrowsException_ThrowSameException() {
        val invitationRequest = createSampleUserInvitationRejectRequest()

        every {
            invitationBo.reject(invitationRequest.id)
        }.throws(Exception("something broke"))

        assertThrows<Exception> {
            invitationController.rejectInvitation(invitationRequest)
        }
    }
}
