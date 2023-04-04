package ca.aversa.insessionservice.service

import ca.aversa.insessionservice.exception.SmsNotificationServiceException
import ca.aversa.insessionservice.util.TestDefaults.TEST_PHONE_NUMBER
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.PublishResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SmsNotificationServiceTest {

    private val sns: AmazonSNS = mockk()
    private val smsNotificationService = SmsNotificationService(sns)

    @Test
    fun testSendSms_CallSnsPublish_SmsSentSuccessfully() {
        val expectedMessageBody = "Heyyyyyyyyyyy"

        every {
            sns.publish(any())
        } returns PublishResult()

        smsNotificationService.sendSms(TEST_PHONE_NUMBER, expectedMessageBody)

        verify {
            sns.publish(PublishRequest()
                .withPhoneNumber(TEST_PHONE_NUMBER)
                .withMessage(expectedMessageBody)
            )
        }
    }

    @Test
    fun testSendSms_SnsThrowsException_ThrowServiceException() {
        every {
            sns.publish(any())
        }.throws(RuntimeException("it broke"))

        assertThrows<SmsNotificationServiceException> {
            smsNotificationService.sendSms(TEST_PHONE_NUMBER, "HEYYYY")
        }
    }
}