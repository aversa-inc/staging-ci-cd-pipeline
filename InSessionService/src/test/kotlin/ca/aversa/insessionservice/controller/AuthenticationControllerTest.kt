package ca.aversa.insessionservice.controller

import ca.aversa.insessionservice.exception.SmsNotificationServiceException
import ca.aversa.insessionservice.service.EmailService
import ca.aversa.insessionservice.service.SmsNotificationService
import ca.aversa.insessionservice.util.Constants
import ca.aversa.insessionservice.util.RegisterAndLoginTestUtils.createSamplePasswordlessRequest
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus

internal class AuthenticationControllerTest {

    private val smsNotificationService: SmsNotificationService = mockk()
    private val emailService: EmailService = mockk()
    private val authenticationController: AuthenticationController = AuthenticationController(smsNotificationService, emailService)

    @Test
    fun testSendSms_ValidPasswordlessRequest_SendSms() {
        val passwordlessRequest = createSamplePasswordlessRequest()

        justRun {
            smsNotificationService.sendSms(passwordlessRequest.phoneNumber, passwordlessRequest.body)
        }

        val response = authenticationController.smsGateway(passwordlessRequest)

        verify {
            smsNotificationService.sendSms(passwordlessRequest.phoneNumber, passwordlessRequest.body)
        }

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun testSendSms_OtpRequestForTestClinician_SendEmail() {
        val passwordlessRequest = createSamplePasswordlessRequest(phoneNumber = Constants.TEST_APPSTORE_CLINICIAN_PHONE)

        justRun {
            emailService.sendEmail(
                Constants.TEST_APPSTORE_CLINICIAN_EMAIL, passwordlessRequest.body, passwordlessRequest.body
            )
        }

        val response = authenticationController.smsGateway(passwordlessRequest)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun testSendSms_OtpRequestForTestClient_SendEmail() {
        val passwordlessRequest = createSamplePasswordlessRequest(phoneNumber = Constants.TEST_APPSTORE_CLIENT_PHONE)

        justRun {
            emailService.sendEmail(
                Constants.TEST_APPSTORE_CLIENT_EMAIL, passwordlessRequest.body, passwordlessRequest.body
            )
        }

        val response = authenticationController.smsGateway(passwordlessRequest)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun testSendSms_SmsNotificationServiceThrowsException_ReturnInternalServerError() {
        val passwordlessRequest = createSamplePasswordlessRequest()

        every {
            smsNotificationService.sendSms(passwordlessRequest.phoneNumber, passwordlessRequest.body)
        }.throws(SmsNotificationServiceException("Can't send SMSSSSSS"))

        assertThrows<Exception> {
            authenticationController.smsGateway(passwordlessRequest)
        }
    }
}