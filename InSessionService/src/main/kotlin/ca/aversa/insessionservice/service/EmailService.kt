package ca.aversa.insessionservice.service

import ca.aversa.insessionservice.exception.EmailServiceException
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.model.Body
import com.amazonaws.services.simpleemail.model.Content
import com.amazonaws.services.simpleemail.model.Destination
import com.amazonaws.services.simpleemail.model.Message
import com.amazonaws.services.simpleemail.model.SendEmailRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class EmailService(private val ses: AmazonSimpleEmailService, domain: String) {

    private val fromAddress: String = "\"Aversa\"<no-reply@$domain>"
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun sendEmail(to: String, subject: String, body: String) {
        log.info("Sending email to $to: $subject from $fromAddress")

        try {
            val sesRequest = SendEmailRequest()
                .withDestination(Destination().withToAddresses(to))
                .withSource(fromAddress)
                .withMessage(Message()
                    .withBody(Body()
                        .withHtml(Content()
                            .withCharset("UTF-8")
                            .withData(body)
                        )
                    )
                    .withSubject(Content()
                        .withCharset("UTF-8")
                        .withData(subject)
                    )
                )

            ses.sendEmail(sesRequest)
        }
        catch (e: Exception) {
            log.error("Exception occurred while sending email: ", e.message)

            throw EmailServiceException(cause = e)
        }
    }
}