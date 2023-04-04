package ca.aversa.insessionservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
data class AppConfig(

    @Value("\${auth0.tenant}")
    val auth0Tenant: String,

    @Value("\${emailDomain}")
    val emailDomain: String,

    @Value("\${siteUrl}")
    val siteUrl: String,

    @Value("\${gcmSnsPlatformApplicationArn}")
    val gcmSnsPlatformApplicationArn: String,

    @Value("\${clinicianRegistrationEmail}")
    val clinicianRegistrationEmail: String
)
