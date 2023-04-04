package ca.aversa.insessionservice.bo

import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.exception.DuplicateResourceException
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.exception.UserProfileUpdateException
import ca.aversa.insessionservice.model.AbstractUser
import ca.aversa.insessionservice.model.entity.AbstractUserTableRow
import ca.aversa.insessionservice.model.entity.ClinicianTableRow
import ca.aversa.insessionservice.model.entity.GcmConfigDocument
import ca.aversa.insessionservice.model.entity.UserCommonTableRow
import ca.aversa.insessionservice.service.Auth0ManagementService
import ca.aversa.insessionservice.service.PushNotificationService
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class UserBo<T: AbstractUser, EntityType: AbstractUserTableRow>(

    private val emailGsiIndexName: String,
    private val phoneGsiIndexName: String,
    private val userDao: BasicDao<EntityType>,
    private val userCommonDao: BasicDao<UserCommonTableRow>,
    private val auth0ManagementService: Auth0ManagementService,
    private val pushNotificationService: PushNotificationService
) {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun doesUserExist(email: String): Boolean {
        return queryEmailGsi(email).isNotEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    fun register(user: T, auth0UserId: String) {
        if(doesUserExist(user.email)) {
            throw DuplicateResourceException("Clinician with email ${user.email} already exists")
        }

        log.info("Registering user: {}", user)

        val userCommonTableRow = UserCommonTableRow(auth0UserId, "${user.firstName} ${user.lastName}")

        update(user.toEntity(auth0UserId) as EntityType)
        userCommonDao.update(userCommonTableRow)

        auth0ManagementService.unblockUser(auth0UserId)
    }

    @Suppress("UNCHECKED_CAST")
    fun updateProfile(userToUpdate: T, auth0UserId: String): T {
        val fieldErrors: MutableMap<String, String> = mutableMapOf()
        val existingUser = userDao.get(auth0UserId)
            ?: throw ResourceNotFoundException("User not found while updating: $auth0UserId")
        val userCommonTableRow = userCommonDao.get(auth0UserId)
            ?: throw ResourceNotFoundException("User common settings not found for user: $auth0UserId")

        if(existingUser.email != userToUpdate.email && doesDuplicateEmailExist(userToUpdate.email, auth0UserId)) {
            fieldErrors["email"] = "User with email address already exists"
        }

        if(existingUser.phoneNumber != userToUpdate.phoneNumber && doesDuplicatePhoneExist(userToUpdate.phoneNumber, auth0UserId)) {
            fieldErrors["phoneNumber"] = "User with phone number already exists"
        }

        if(fieldErrors.isNotEmpty()) {
            throw UserProfileUpdateException(fieldErrors = fieldErrors)
        }

        val updatedUserCommonTableRow = userCommonTableRow.copy(
            userFriendlyIdentifier = "${userToUpdate.firstName} ${userToUpdate.lastName}"
        )

        val userToUpdateTableRow = userToUpdate.toEntity(auth0UserId)

        if(existingUser is ClinicianTableRow) {
            (userToUpdateTableRow as ClinicianTableRow).clients = existingUser.clients;
        }

        update(userToUpdateTableRow as EntityType)
        userCommonDao.update(updatedUserCommonTableRow)
        auth0ManagementService.updateUserPhoneNumber(auth0UserId, userToUpdate.phoneNumber)

        return userToUpdate
    }

    fun updateFcmToken(auth0UserId: String, fcmDeviceToken: String) {
        val existingUser = userCommonDao.get(auth0UserId)
            ?: throw ResourceNotFoundException("User common settings not found for user: $auth0UserId")

        val endpointArn = getUpdatedEndpointArn(fcmDeviceToken)
        val gcmConfig = GcmConfigDocument(endpointArn)

        val updatedUser = existingUser.copy(gcmConfigs = existingUser.gcmConfigs + gcmConfig)

        userCommonDao.update(updatedUser)
    }

    private fun getUpdatedEndpointArn(newToken: String): String {
        return pushNotificationService.updateAndGetPlatformEndpointArn(newToken)
    }

    private fun update(userToUpdate: EntityType) {
        log.info("Updating user profile: {}", userToUpdate)

        userDao.update(userToUpdate)
    }

    private fun queryEmailGsi(email: String): List<EntityType> {
        val keyConditionExpression = "email = :email"
        val valuesMap = mutableMapOf<String, AttributeValue>(
            ":email" to AttributeValue().withS(email)
        )

        return userDao.queryGsi(emailGsiIndexName, keyConditionExpression, valuesMap)
    }

    private fun queryPhoneGsi(phoneNumber: String): List<EntityType> {
        val keyConditionExpression = "phone_number = :phone_number"
        val valuesMap = mutableMapOf<String, AttributeValue>(
            ":phone_number" to AttributeValue().withS(phoneNumber)
        )

        return userDao.queryGsi(phoneGsiIndexName, keyConditionExpression, valuesMap)
    }

    private fun doesDuplicateEmailExist(email: String, auth0UserId: String): Boolean {
        return queryEmailGsi(email).any { row ->
            row.id != auth0UserId
        }
    }

    private fun doesDuplicatePhoneExist(phoneNumber: String, auth0UserId: String): Boolean {
        return queryPhoneGsi(phoneNumber).any{ row ->
            row.id != auth0UserId
        }
    }
}