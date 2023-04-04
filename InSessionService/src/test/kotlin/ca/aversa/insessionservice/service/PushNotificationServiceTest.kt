package ca.aversa.insessionservice.service

import ca.aversa.insessionservice.exception.PushNotificationServiceException
import ca.aversa.insessionservice.model.entity.GcmNotificationWrapper
import ca.aversa.insessionservice.model.entity.GcmSnsMessage
import ca.aversa.insessionservice.util.TestDefaults.TEST_GCM_TOKEN
import ca.aversa.insessionservice.util.TestDefaults.TEST_GCM_TOKEN_ENDPOINT_ARN
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.PublishResult
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PushNotificationServiceTest {

    private val gcmPlatformAppArn = "gcmPlatformArn"
    private val gcmToken = TEST_GCM_TOKEN
    private val gcmEndpointArn = TEST_GCM_TOKEN_ENDPOINT_ARN
    private val sns: AmazonSNS = mockk()
    private val gson: Gson = mockk()
    private val pushNotificationService = PushNotificationService(gcmPlatformAppArn, sns, gson)

    @Test
    fun testUpdateAndGetPlatformEndpoint_UpdateNewPlatformEndpoint_ReturnEndpointArn() {
        val request = createSampleCreatePlatformEndpointRequest()
        val expectedResponse = createSampleCreatePlatformEndpointResponse()

        every {
            sns.createPlatformEndpoint(request)
        }.returns(expectedResponse)

        val actualEndpointArn = pushNotificationService.updateAndGetPlatformEndpointArn(gcmToken)

        assertEquals(expectedResponse.endpointArn, actualEndpointArn)
    }

    @Test
    fun testUpdateAndGetPlatformEndpoint_SnsThrowsException_ThrowServiceException() {
        val request = createSampleCreatePlatformEndpointRequest()

        every {
            sns.createPlatformEndpoint(request)
        }.throws(RuntimeException("Something broke"))

        assertThrows<PushNotificationServiceException> {
            pushNotificationService.updateAndGetPlatformEndpointArn(gcmToken)
        }
    }

    @Test
    fun testSendNotification_CallSnsPublish_NotificationSentSuccessfully() {
        val expectedMessage = "notification payload"
        val request = createSampleSnsPublishRequest(expectedMessage)

        every {
            gson.toJson(any<GcmNotificationWrapper>())
        }.returns("notification wrapper")

        every {
            gson.toJson(any<GcmSnsMessage>())
        }.returns(expectedMessage)

        every {
            sns.publish(request)
        }.returns(PublishResult())

        pushNotificationService.sendNotification(gcmEndpointArn, "some title", "some body")
    }

    @Test
    fun testSendNotification_SnsThrowsException_ThrowServiceException() {
        val expectedMessage = "notification payload"
        val request = createSampleSnsPublishRequest(expectedMessage)

        every {
            gson.toJson(any<GcmNotificationWrapper>())
        }.returns("notification wrapper")

        every {
            gson.toJson(any<GcmSnsMessage>())
        }.returns(expectedMessage)

        every {
            sns.publish(request)
        }.throws(RuntimeException("Something broke"))

        assertThrows<PushNotificationServiceException> {
            pushNotificationService.sendNotification(gcmEndpointArn, "some title", "some body")
        }
    }

    private fun createSampleCreatePlatformEndpointRequest(): CreatePlatformEndpointRequest {
        return CreatePlatformEndpointRequest()
            .withToken(gcmToken)
            .withPlatformApplicationArn(gcmPlatformAppArn)
    }

    private fun createSampleCreatePlatformEndpointResponse(): CreatePlatformEndpointResult {
        return CreatePlatformEndpointResult()
            .withEndpointArn("arn:aws/somearn")
    }

    private fun createSampleSnsPublishRequest(message: String): PublishRequest {
        val request = PublishRequest()
        request.withMessage(message)
        request.withTargetArn(gcmEndpointArn)
        request.withMessageStructure("json")

        return request
    }
}