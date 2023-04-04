package ca.aversa.insessionservice.service

import ca.aversa.insessionservice.exception.SmsNotificationServiceException
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class SmsNotificationService(private val sns: AmazonSNS) {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun sendSms(phoneNumber: String, body: String) {
        try {
            log.info("Sending sms to {}: {}", phoneNumber, body)

            val publishRequest = PublishRequest()
            publishRequest.phoneNumber = phoneNumber
            publishRequest.message = body

            sns.publish(publishRequest)
        }
        catch (e: Exception) {
            throw SmsNotificationServiceException(cause = e)
        }
    }
}