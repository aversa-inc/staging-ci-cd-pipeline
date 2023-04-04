package ca.aversa.insessionservice.service

import ca.aversa.insessionservice.exception.PushNotificationServiceException
import ca.aversa.insessionservice.model.entity.GcmNotificationPayload
import ca.aversa.insessionservice.model.entity.GcmNotificationWrapper
import ca.aversa.insessionservice.model.entity.GcmSnsMessage
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest
import com.amazonaws.services.sns.model.EndpointDisabledException
import com.amazonaws.services.sns.model.PublishRequest
import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class PushNotificationService(

    private val gcmPlatformAppArn: String,
    private val sns: AmazonSNS,
    private val gson: Gson
) {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun updateAndGetPlatformEndpointArn(token: String): String {
        try {
            val createPlatformEndpointRequest = CreatePlatformEndpointRequest()
                .withToken(token)
                .withPlatformApplicationArn(gcmPlatformAppArn)

            val endpointResult = sns.createPlatformEndpoint(createPlatformEndpointRequest)

            return endpointResult.endpointArn
        }
        catch (e: Exception) {
            log.error("Failed to create GCM endpoint for token: $token")

            throw PushNotificationServiceException(cause = e)
        }
    }

    fun sendNotification(endpointArn: String, title: String, body: String) {
        try {
            val request = PublishRequest()
            request.withTargetArn(endpointArn)
            request.withMessage(createGcmMessage(title, body))
            request.withMessageStructure("json")

            sns.publish(request)
        }
        catch (e: Exception) {
            when(e) {
                !is EndpointDisabledException -> throw PushNotificationServiceException(cause = e)
            }
        }
    }

    private fun createGcmMessage(title: String, body: String): String {
        val payload = GcmNotificationWrapper(
            GcmNotificationPayload(title, body)
        )
        val snsMessage = GcmSnsMessage(gson.toJson(payload))

        return gson.toJson(snsMessage)
    }
}