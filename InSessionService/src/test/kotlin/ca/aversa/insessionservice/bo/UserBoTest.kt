package ca.aversa.insessionservice.bo

import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.exception.DuplicateResourceException
import ca.aversa.insessionservice.exception.PushNotificationServiceException
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.exception.UserProfileUpdateException
import ca.aversa.insessionservice.model.Clinician
import ca.aversa.insessionservice.model.entity.ClinicianTableRow
import ca.aversa.insessionservice.model.entity.UserCommonTableRow
import ca.aversa.insessionservice.service.Auth0ManagementService
import ca.aversa.insessionservice.service.PushNotificationService
import ca.aversa.insessionservice.util.TestDefaults
import ca.aversa.insessionservice.util.UserTestUtils
import ca.aversa.insessionservice.util.UserTestUtils.createSampleClient
import ca.aversa.insessionservice.util.UserTestUtils.createSampleGcmConfigDocument
import ca.aversa.insessionservice.util.UserTestUtils.createSampleUserCommonTableRow
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.auth0.json.mgmt.users.User
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class UserBoTest {

    // Use Clinician as a type for the Bo/Dao as the Bo/Dao should work on other user types as well
    private val userDao: BasicDao<ClinicianTableRow> = mockk()
    private val userCommonDao: BasicDao<UserCommonTableRow> = mockk()
    private val auth0ManagementService: Auth0ManagementService = mockk()
    private val pushNotificationService: PushNotificationService = mockk()
    private val emailGsiIndexName = "email_index"
    private val phoneGsiIndexName = "phone_number_index"
    private val clinician = UserTestUtils.createSampleClinician()
    private val auth0UserId = "auth0user"
    private val emailGsiKeyExpression = "email = :email"
    private val phoneGsiKeyExpression = "phone_number = :phone_number"
    private val gcmToken = TestDefaults.TEST_GCM_TOKEN
    private val gcmEndpointArn = TestDefaults.TEST_GCM_TOKEN_ENDPOINT_ARN
    private val userBo: UserBo<Clinician, ClinicianTableRow> = UserBo(
        emailGsiIndexName, phoneGsiIndexName, userDao, userCommonDao, auth0ManagementService, pushNotificationService
    )

    @Test
    fun testUpdate_UpdateClientAttributes_NoExceptionThrown() {
        val userCommonTableRow = createSampleUserCommonTableRow(
            auth0UserId, "${clinician.firstName} ${clinician.lastName}", gcmConfigs = emptyList()
        )
        val existingUser = clinician.toEntity(auth0UserId)
        existingUser.clients = mutableListOf(createSampleClient().auth0UserId!!)

        val userToUpdate = clinician.toEntity(auth0UserId)
        userToUpdate.clients = existingUser.clients;

        every {
            userDao.get(auth0UserId)
        }.returns(existingUser)

        every {
            userCommonDao.get(auth0UserId)
        }.returns(userCommonTableRow)

        every {
            auth0ManagementService.updateUserPhoneNumber(auth0UserId, clinician.phoneNumber)
        }.returns(clinician.toAuth0User())

        justRun {
            userDao.update(userToUpdate)
        }

        justRun {
            userCommonDao.update(userCommonTableRow)
        }

        userBo.updateProfile(clinician, auth0UserId)
    }

    @Test
    fun testUpdate_AddingToDynamoDbThrowsException_ThrowException() {
        every {
            userDao.get(auth0UserId)
        }.returns(clinician.toEntity(auth0UserId))

        every {
            userDao.update(any())
        }.throws(RuntimeException("Something broke"))

        assertThrows<RuntimeException> {
            userBo.updateProfile(clinician, auth0UserId)
        }
    }

    @Test
    fun testUpdate_ExistingUserNotFound_ThrowUserProfileUpdateException() {
        every {
            userDao.get(auth0UserId)
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            userBo.updateProfile(clinician, auth0UserId)
        }
    }

    @Test
    fun testUpdate_NewEmailGivenAndItDoesntExist_UpdateProfile() {
        val nonDuplicateClinician = UserTestUtils.createSampleClinician(
            firstName = "newfirstname", lastName = "newlastname", email = "someotheremail@email.com"
        )
        val userCommonTableRow = createSampleUserCommonTableRow(
            auth0UserId, "${clinician.firstName} ${clinician.lastName}", gcmConfigs = emptyList()
        )
        val emailGsiValuesMap = mapOf<String, AttributeValue>(
            ":email" to AttributeValue().withS(clinician.email)
        )

        every {
            userDao.get(auth0UserId)
        }.returns(clinician.toEntity(auth0UserId))

        every {
            userDao.queryGsi(emailGsiIndexName, emailGsiKeyExpression, emailGsiValuesMap)
        }.returns(listOf(nonDuplicateClinician.toEntity(auth0UserId)))

        every {
            userCommonDao.get(auth0UserId)
        }.returns(userCommonTableRow)

        every {
            auth0ManagementService.updateUserPhoneNumber(auth0UserId, clinician.phoneNumber)
        }.returns(clinician.toAuth0User())

        justRun {
            userDao.update(clinician.toEntity(auth0UserId))
        }

        justRun {
            userCommonDao.update(userCommonTableRow)
        }

        userBo.updateProfile(clinician, auth0UserId)
    }

    @Test
    fun testUpdate_NewEmailGivenAndItAlreadyExist_DontUpdateProfile() {
        val alreadyExistingEmail = "alreadyexistingemail@email.com"
        val duplicateClinician = UserTestUtils.createSampleClinician(email = alreadyExistingEmail)
        val clinicianToEdit = UserTestUtils.createSampleClinician(email = alreadyExistingEmail)
        val emailGsiValuesMap = mapOf<String, AttributeValue>(
            ":email" to AttributeValue().withS(alreadyExistingEmail)
        )
        val userCommonTableRow = createSampleUserCommonTableRow(
            auth0UserId,
            "${duplicateClinician.firstName} ${duplicateClinician.lastName}", gcmConfigs = emptyList()
        )

        every {
            userDao.get(auth0UserId)
        }.returns(clinician.toEntity(auth0UserId))

        every {
            userCommonDao.get(auth0UserId)
        }.returns(userCommonTableRow)

        every {
            userDao.queryGsi(emailGsiIndexName, emailGsiKeyExpression, emailGsiValuesMap)
        }.returns(listOf(duplicateClinician.toEntity("someuserid")))

        val exception = assertThrows<UserProfileUpdateException> {
            userBo.updateProfile(clinicianToEdit, auth0UserId)
        }

        assertNotNull(exception.fieldErrors)
        assertTrue(exception.fieldErrors!!.containsKey("email"))
    }

    @Test
    fun testUpdate_NewPhoneGivenAndItDoesntExist_UpdateProfile() {
        val nonDuplicateClinician = UserTestUtils.createSampleClinician(phoneNumber = "1111111111")
        val phoneGsiValueMap = mapOf<String, AttributeValue>(
            ":phone_number" to AttributeValue().withS(clinician.phoneNumber)
        )
        val userCommonTableRow = createSampleUserCommonTableRow(
            auth0UserId,
            "${nonDuplicateClinician.firstName} ${nonDuplicateClinician.lastName}", gcmConfigs = emptyList()
        )

        every {
            userDao.get(auth0UserId)
        }.returns(clinician.toEntity(auth0UserId))

        every {
            userCommonDao.get(auth0UserId)
        }.returns(userCommonTableRow)

        every {
            userDao.queryGsi(phoneGsiIndexName, phoneGsiKeyExpression, phoneGsiValueMap)
        }.returns(listOf(nonDuplicateClinician.toEntity(auth0UserId)))

        every {
            auth0ManagementService.updateUserPhoneNumber(auth0UserId, clinician.phoneNumber)
        }.returns(clinician.toAuth0User())

        justRun {
            userDao.update(nonDuplicateClinician.toEntity(auth0UserId))
        }

        justRun {
            userCommonDao.update(userCommonTableRow)
        }

        userBo.updateProfile(clinician, auth0UserId)
    }

    @Test
    fun testUpdate_NewPhoneGivenAndItAlreadyExist_DontUpdateProfile() {
        val alreadyExistingPhone = "2222222222"
        val duplicateClinician = UserTestUtils.createSampleClinician(phoneNumber = alreadyExistingPhone)
        val clinicianToEdit = UserTestUtils.createSampleClinician(phoneNumber = alreadyExistingPhone)
        val phoneGsiValueMap = mapOf<String, AttributeValue>(
            ":phone_number" to AttributeValue().withS(alreadyExistingPhone)
        )
        val userCommonTableRow = createSampleUserCommonTableRow(
            auth0UserId,
            "${duplicateClinician.firstName} ${duplicateClinician.lastName}", gcmConfigs = emptyList()
        )

        every {
            userDao.get(auth0UserId)
        }.returns(clinician.toEntity(auth0UserId))

        every {
            userCommonDao.get(auth0UserId)
        }.returns(userCommonTableRow)

        every {
            userDao.queryGsi(phoneGsiIndexName, phoneGsiKeyExpression, phoneGsiValueMap)
        }.returns(listOf(duplicateClinician.toEntity("someuserid")))

        val exception = assertThrows<UserProfileUpdateException> {
            userBo.updateProfile(clinicianToEdit, auth0UserId)
        }

        assertNotNull(exception.fieldErrors)
        assertTrue(exception.fieldErrors!!.containsKey("phoneNumber"))
    }

    @Test
    fun testRegister_UserDoesNotExist_AddUserAndReturnTrue() {
        val userCommonTableRow = createSampleUserCommonTableRow(
            auth0UserId, "${clinician.firstName} ${clinician.lastName}", gcmConfigs = emptyList()
        )
        val emailGsiValuesMap = mapOf<String, AttributeValue>(
            ":email" to AttributeValue().withS(clinician.email)
        )

        every {
            userDao.queryGsi(emailGsiIndexName, emailGsiKeyExpression, emailGsiValuesMap)
        }.returns(emptyList())

        every {
            auth0ManagementService.unblockUser(auth0UserId)
        }.returns(User())

        justRun {
            userDao.update(clinician.toEntity(auth0UserId))
        }

        justRun {
            userCommonDao.update(userCommonTableRow)
        }

        userBo.register(clinician, auth0UserId)
    }

    @Test
    fun testRegister_UserDoesExists_DontUpdateUserAndReturnFalse() {
        val emailGsiValuesMap = mapOf<String, AttributeValue>(
            ":email" to AttributeValue().withS(clinician.email)
        )

        every {
            userDao.queryGsi(emailGsiIndexName, emailGsiKeyExpression, emailGsiValuesMap)
        }.returns(listOf(clinician.toEntity(auth0UserId)))

        assertThrows<DuplicateResourceException> {
            userBo.register(clinician, auth0UserId)
        }
    }

    @Test
    fun testRegister_DynamoDbGetThrowsException_ThrowRegisterUserException() {
        every {
            userDao.get(any())
        }.throws(RuntimeException("kjsdhfjhksdjkf"))

        assertThrows<RuntimeException> {
            userBo.register(clinician, auth0UserId)
        }

        verify(exactly = 0) {
            userDao.update(clinician.toEntity(auth0UserId))
            auth0ManagementService.unblockUser(auth0UserId)
        }
    }

    @Test
    fun testRegister_DynamoDbUpdateThrowsException_ThrowRegisterUserException() {
        every {
            userDao.get(any())
        }.returns(null)

        every {
            userDao.update(any())
        }.throws(RuntimeException("UPDATE FAILED!!!!!!!!!!!!!!!"))

        assertThrows<RuntimeException> {
            userBo.register(clinician, auth0UserId)
        }
    }

    @Test
    fun testRegister_Auth0ManagementServiceThrowsException_ThrowRegisterUserException() {
        every {
            userDao.get(any())
        }.returns(null)

        justRun {
            userDao.update(any())
        }

        every {
            auth0ManagementService.unblockUser(auth0UserId)
        }.throws(RuntimeException("Something broke while adding to auth0"))

        assertThrows<RuntimeException> {
            userBo.register(clinician, auth0UserId)
        }
    }

    @Test
    fun testUpdateFcmToken_ExistingUserFound_NewFcmTokenUpdated() {
        val userCommonTableRow = createSampleUserCommonTableRow()
        val updatedUserCommonTableRow = userCommonTableRow.copy(
            gcmConfigs = userCommonTableRow.gcmConfigs + createSampleGcmConfigDocument()
        )

        every {
            userCommonDao.get(auth0UserId)
        }.returns(userCommonTableRow)

        every {
            pushNotificationService.updateAndGetPlatformEndpointArn(gcmToken)
        }.returns(gcmEndpointArn)

        justRun {
            userCommonDao.update(updatedUserCommonTableRow)
        }

        userBo.updateFcmToken(auth0UserId, gcmToken)
    }

    @Test
    fun testUpdateFcmToken_UserNotFound_ThrowResourceNotFoundException() {
        every {
            userCommonDao.get(auth0UserId)
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            userBo.updateFcmToken(auth0UserId, gcmToken)
        }
    }

    @Test
    fun testUpdateFcmToken_PushNotificationServiceThrowsException_ThrowExceptionAndDontAddToDynamoDb() {
        val userCommonTableRow = createSampleUserCommonTableRow()

        every {
            userCommonDao.get(auth0UserId)
        }.returns(userCommonTableRow)

        every {
            pushNotificationService.updateAndGetPlatformEndpointArn(gcmToken)
        }.throws(PushNotificationServiceException("something broke"))

        assertThrows<PushNotificationServiceException> {
            userBo.updateFcmToken(auth0UserId, gcmToken)
        }
    }

    @Test
    fun testUpdateFcmToken_DynamoDbThrowsExceptionWhileUpdating_ThrowException() {
        val clinicianTableRow = clinician.toEntity(auth0UserId)
        val userCommonTableRow = createSampleUserCommonTableRow(auth0UserId)

        every {
            userDao.get(auth0UserId)
        }.returns(clinicianTableRow)

        every {
            userCommonDao.update(userCommonTableRow)
        }.throws(RuntimeException("Something broke"))

        assertThrows<RuntimeException> {
            userBo.updateFcmToken(auth0UserId, gcmToken)
        }
    }
}