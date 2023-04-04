package ca.aversa.insessionservice.service

import ca.aversa.insessionservice.exception.EmailServiceException
import ca.aversa.insessionservice.util.TestDefaults.TEST_EMAIL
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.model.Body
import com.amazonaws.services.simpleemail.model.Content
import com.amazonaws.services.simpleemail.model.Destination
import com.amazonaws.services.simpleemail.model.Message
import com.amazonaws.services.simpleemail.model.SendEmailRequest
import com.amazonaws.services.simpleemail.model.SendEmailResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class EmailServiceTest {

    private val emailDomain = "something.com"
    private val expectedFromAddress = "\"Aversa\"<no-reply@$emailDomain>"
    private val ses: AmazonSimpleEmailService = mockk()
    private val emailService: EmailService = EmailService(ses, emailDomain)

    @Test
    fun testSendEmail_CallSesToSendEmail_EmailSentSuccessfully() {
        val expectedSubject = "email subject"
        val expectedBody = "email body"

        every {
            ses.sendEmail(any())
        }.returns(SendEmailResult())

        emailService.sendEmail(TEST_EMAIL, expectedSubject, expectedBody)

        verify {
            ses.sendEmail(
                createSendEmailRequest(TEST_EMAIL, expectedSubject, expectedBody)
            )
        }
    }

    @Test
    fun testSendEmail_SesThrowsException_ThrowServiceException() {
        val expectedSubject = "email subject"
        val expectedBody = "email body"

        every {
            ses.sendEmail(any())
        }.throws(RuntimeException("it broke"))

        assertThrows<EmailServiceException> {
            emailService.sendEmail(TEST_EMAIL, expectedSubject, expectedBody)
        }
    }

    private fun createSendEmailRequest(to: String, subject: String, body: String): SendEmailRequest {
        return SendEmailRequest()
            .withDestination(Destination().withToAddresses(to))
            .withSource(expectedFromAddress)
            .withMessage(
                Message()
                    .withBody(
                        Body()
                            .withHtml(
                                Content()
                                    .withCharset("UTF-8")
                                    .withData(body)
                            )
                    )
                    .withSubject(
                        Content()
                            .withCharset("UTF-8")
                            .withData(subject)
                    )
            )
    }
}