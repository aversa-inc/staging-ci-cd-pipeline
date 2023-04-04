package ca.aversa.insessionservice.service

import ca.aversa.insessionservice.exception.Auth0ManagementServiceException
import ca.aversa.insessionservice.model.entity.Auth0ManagementApiToken
import ca.aversa.insessionservice.util.EnvironmentUtils
import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.auth0.client.mgmt.ManagementAPI
import com.auth0.client.mgmt.filter.UserFilter
import com.auth0.exception.APIException
import com.auth0.exception.Auth0Exception
import com.auth0.json.mgmt.users.User
import com.auth0.net.Request
import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.EnableRetry
import org.springframework.retry.annotation.Retryable
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@EnableScheduling
@EnableRetry
class Auth0ManagementService(

    private val secretsManager: AWSSecretsManager,
    private val gson: Gson,
    private val managementAPI: ManagementAPI
) {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    private val tokenSecretName: String = EnvironmentUtils.extractEnvironmentVariable("AUTH0_TOKEN_SECRET_NAME")

    @Retryable(value = [APIException::class], backoff = Backoff(delay = 100))
    fun addUser(user: User): User {
        val existingUser = getUserByPhoneNumber(user.phoneNumber)

        if(existingUser != null) {
            return existingUser
        }

        user.isPhoneVerified = true
        user.isBlocked = true

        val request = managementAPI.users().create(user)

        log.info("Adding user to Auth0: $user")

        return executeRequest(request)
    }

    @Retryable(value = [APIException::class], backoff = Backoff(delay = 100))
    fun getUserByPhoneNumber(phoneNumber: String): User? {
        val request = managementAPI.users().list(UserFilter().withQuery("phone_number:\"$phoneNumber\""))
        val results = executeRequest(request)

        if(results.items.isEmpty()) {
            return null
        }

        return results.items[0]
    }

    @Retryable(value = [APIException::class], backoff = Backoff(delay = 100))
    fun getUserByEmail(email: String): User? {
        val request = managementAPI.users().listByEmail(email, null)
        val results = executeRequest(request)

        if(results.isEmpty()) {
            return null
        }

        return results[0]
    }

    @Retryable(value = [APIException::class], backoff = Backoff(delay = 100))
    fun unblockUser(auth0UserId: String): User {
        val user = User()
        user.isBlocked = false

        val request = managementAPI.users().update(auth0UserId, user)

        log.info("Unblocking user {}", auth0UserId)

        return executeRequest(request)
    }

    @Retryable(value = [APIException::class], backoff = Backoff(delay = 100))
    fun updateUserPhoneNumber(auth0UserId: String, phoneNumber: String): User {
        val user = User()
        user.phoneNumber = phoneNumber

        val request = managementAPI.users().update(auth0UserId, user)

        log.info("Updating phone for user {}", auth0UserId)

        return executeRequest(request)
    }

    @Retryable(value = [APIException::class], backoff = Backoff(delay = 100))
    fun deleteUser(auth0UserId: String) {
        val request = managementAPI.users().delete(auth0UserId)

        log.info("Deleting user {}", auth0UserId)

        executeRequest(request)
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
    protected fun refreshApiToken() {
        log.info("Fetching auth0 management api token")

        val getSecretValueRequest = GetSecretValueRequest()
        getSecretValueRequest.secretId = tokenSecretName

        try {
            val getSecretValueResponse = secretsManager.getSecretValue(getSecretValueRequest)
            val secretString = getSecretValueResponse.secretString
            val auth0ManagementApiToken = gson.fromJson(secretString, Auth0ManagementApiToken::class.java)

            managementAPI.setApiToken(auth0ManagementApiToken.accessToken)
        }
        catch (e: Exception) {
            log.error("Failed to fetch auth0 management api token from Secrets Manager: ", e)
        }
    }

    private fun <T> executeRequest(request: Request<T>): T {
        try {
            return request.execute()
        }
        catch (e: APIException) {
            log.warn("APIException while adding user to Auth0: ${e.description}")

            throw e
        }
        catch (e: Auth0Exception) {
            log.error("Auth0Exception while adding user to Auth0: ${e.message}")

            throw Auth0ManagementServiceException(cause = e)
        }
    }
}