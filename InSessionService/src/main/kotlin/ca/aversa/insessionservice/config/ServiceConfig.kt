package ca.aversa.insessionservice.config

import ca.aversa.insessionservice.service.EmailService
import ca.aversa.insessionservice.service.PushNotificationService
import ca.aversa.insessionservice.service.SmsNotificationService
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.sns.AmazonSNS
import com.auth0.client.mgmt.ManagementAPI
import com.google.gson.Gson
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class ServiceConfig {

    @Bean
    fun getSmsNotificationService(sns: AmazonSNS): SmsNotificationService {
        return SmsNotificationService(sns)
    }

    @Bean
    fun getGson(): Gson {
        return Gson()
    }

    @Bean
    fun getAuth0ManagementApiClient(appConfig: AppConfig): ManagementAPI {
        // Initialize with empty token since Auth0ManagementService will internally manage the token state
        return ManagementAPI(appConfig.auth0Tenant, "")
    }

    @Bean
    fun getEmailService(ses: AmazonSimpleEmailService, appConfig: AppConfig): EmailService {
        return EmailService(ses, appConfig.emailDomain)
    }

    @Bean
    fun getPushNotificationService(appConfig: AppConfig, sns: AmazonSNS, gson: Gson): PushNotificationService {
        return PushNotificationService(appConfig.gcmSnsPlatformApplicationArn, sns, gson)
    }
}