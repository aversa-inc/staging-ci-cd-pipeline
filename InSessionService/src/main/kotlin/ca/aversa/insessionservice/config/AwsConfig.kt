package ca.aversa.insessionservice.config

import ca.aversa.insessionservice.util.EnvironmentUtils
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.Regions
import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class AwsConfig {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun getSnsClient(awsRegions: Regions): AmazonSNS {
        return AmazonSNSClientBuilder.standard()
            .withRegion(awsRegions)
            .build();
    }

    @Bean
    fun getAwsRegion(): Regions {
        val regionName = EnvironmentUtils.extractEnvironmentVariable("AWS_REGION")

        log.info("Identified region from environment variables: {}", regionName)

        return Regions.fromName(regionName)
    }

    @Bean
    fun getSecretsManager(awsRegions: Regions): AWSSecretsManager {
        return AWSSecretsManagerClient.builder()
            .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
            .build()
    }

    @Bean
    fun getSesClient(awsRegions: Regions): AmazonSimpleEmailService {
        return AmazonSimpleEmailServiceClientBuilder.standard()
            .withRegion(awsRegions)
            .build();
    }
}