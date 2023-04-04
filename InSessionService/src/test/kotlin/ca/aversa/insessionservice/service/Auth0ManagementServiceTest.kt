package ca.aversa.insessionservice.service

import ca.aversa.insessionservice.exception.Auth0ManagementServiceException
import ca.aversa.insessionservice.model.entity.Auth0ManagementApiToken
import ca.aversa.insessionservice.util.Auth0TestUtils.createSampleAuth0User
import ca.aversa.insessionservice.util.Auth0TestUtils.createSampleAuth0UserAddResponse
import ca.aversa.insessionservice.util.EnvironmentUtils
import ca.aversa.insessionservice.util.TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID
import ca.aversa.insessionservice.util.TestDefaults.TEST_EMAIL
import ca.aversa.insessionservice.util.TestDefaults.TEST_PHONE_NUMBER
import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult
import com.auth0.client.mgmt.ManagementAPI
import com.auth0.exception.APIException
import com.auth0.exception.Auth0Exception
import com.auth0.json.mgmt.users.User
import com.auth0.json.mgmt.users.UsersPage
import com.auth0.net.Request
import com.google.gson.Gson
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class Auth0ManagementServiceTest {

    private val secretsManager: AWSSecretsManager = mockk()
    private val gson: Gson = mockk()
    private val managementApi: ManagementAPI = mockk()

    private lateinit var auth0ManagementService: Auth0ManagementService

    @BeforeEach
    fun setup() {
        mockkObject(EnvironmentUtils)

        every {
            EnvironmentUtils.extractEnvironmentVariable(any())
        }.returns("auth0-token-secret-name")

        auth0ManagementService = Auth0ManagementService(secretsManager, gson, managementApi)
    }

    @Test
    fun testAddUser_SuccessfullyAddsUserToAuth0WhenTheyDontExist_ReturnAddedUser() {
        val userToAdd = createSampleAuth0User()
        val expectedUser = createSampleAuth0UserAddResponse()
        val request = mockk<Request<User>>()
        val expectedUserList = emptyList<User>()
        val usersPageResponse = UsersPage(expectedUserList)
        val expectedUserRequest = mockk<Request<UsersPage>>()

        every {
            expectedUserRequest.execute()
        }.returns(usersPageResponse)

        every {
            managementApi.users().list(any())
        }.returns(expectedUserRequest)

        every {
            request.execute()
        }.returns(expectedUser)

        every {
            managementApi.users().create(userToAdd)
        }.returns(request)

        val actualUser = auth0ManagementService.addUser(userToAdd)

        assertUser(expectedUser, actualUser)
    }

    @Test
    fun testAddUser_DontAddUserIfTheyAlreadyExist_ReturnExistingUser() {
        val userToAdd = createSampleAuth0User()
        val expectedUser = createSampleAuth0UserAddResponse()
        val existingUser = createSampleAuth0User()
        val request = mockk<Request<User>>()
        val expectedUserList = listOf(existingUser)
        val usersPageResponse = UsersPage(expectedUserList)
        val expectedUserRequest = mockk<Request<UsersPage>>()

        every {
            expectedUserRequest.execute()
        }.returns(usersPageResponse)

        every {
            managementApi.users().list(any())
        }.returns(expectedUserRequest)

        val actualUser = auth0ManagementService.addUser(userToAdd)

        assertUser(existingUser, actualUser)
    }

    @Test
    fun testAddUser_CallingAuth0ThrowsAPIException_ReturnAPIException() {
        val userToAdd = createSampleAuth0User()
        val request = mockk<Request<User>>()
        val expectedUserList = emptyList<User>()
        val usersPageResponse = UsersPage(expectedUserList)
        val expectedUserRequest = mockk<Request<UsersPage>>()

        every {
            expectedUserRequest.execute()
        }.returns(usersPageResponse)

        every {
            managementApi.users().list(any())
        }.returns(expectedUserRequest)

        every {
            request.execute()
        }.throws(APIException("duplicate user", 400, RuntimeException("duplicate user")))

        every {
            managementApi.users().create(userToAdd)
        }.returns(request)

        assertThrows<APIException> {
            auth0ManagementService.addUser(userToAdd)
        }
    }

    @Test
    fun testAddUser_CallingAuth0ThrowsAuth0Exception_ThrowServiceException() {
        val userToAdd = createSampleAuth0User()
        val request = mockk<Request<User>>()
        val expectedUserList = emptyList<User>()
        val usersPageResponse = UsersPage(expectedUserList)
        val expectedUserRequest = mockk<Request<UsersPage>>()

        every {
            expectedUserRequest.execute()
        }.returns(usersPageResponse)

        every {
            managementApi.users().list(any())
        }.returns(expectedUserRequest)

        every {
            request.execute()
        }.throws(Auth0Exception("Something broke"))

        every {
            managementApi.users().create(userToAdd)
        }.returns(request)

        assertThrows<Auth0ManagementServiceException> {
            auth0ManagementService.addUser(userToAdd)
        }
    }

    @Test
    fun testGetUserByPhoneNumber_ManagementApiReturnsUser_ReturnFirstUser() {
        val expectedUserList = listOf(createSampleAuth0User())
        val usersPageResponse = UsersPage(expectedUserList)
        val request = mockk<Request<UsersPage>>()

        every {
            request.execute()
        }.returns(usersPageResponse)

        every {
            managementApi.users().list(any())
        }.returns(request)

        val actualUser = auth0ManagementService.getUserByPhoneNumber(TEST_PHONE_NUMBER)

        assertNotNull(actualUser)
        assertEquals(expectedUserList[0], actualUser)
    }

    @Test
    fun testGetUserByPhoneNumber_ManagementApiReturnsNoUsers_ReturnNull() {
        val expectedUserList = emptyList<User>()
        val usersPageResponse = UsersPage(expectedUserList)
        val request = mockk<Request<UsersPage>>()

        every {
            request.execute()
        }.returns(usersPageResponse)

        every {
            managementApi.users().list(any())
        }.returns(request)

        val actualUser = auth0ManagementService.getUserByPhoneNumber(TEST_PHONE_NUMBER)

        assertNull(actualUser)
    }

    @Test
    fun testGetUserByPhoneNumber_CallingAuth0ThrowsApiException_ThrowApiException() {
        val request = mockk<Request<UsersPage>>()

        every {
            request.execute()
        }.throws(APIException("Something broke", 500, RuntimeException("Something broke")))

        every {
            managementApi.users().list(any())
        }.returns(request)

        assertThrows<APIException> {
            auth0ManagementService.getUserByPhoneNumber(TEST_PHONE_NUMBER)
        }
    }

    @Test
    fun testGetUserByPhoneNumber_CallingAuth0ThrowsAuth0Exception_ThrowServiceException() {
        val request = mockk<Request<UsersPage>>()

        every {
            request.execute()
        }.throws(Auth0Exception("something broke"))

        every {
            managementApi.users().list(any())
        }.returns(request)

        assertThrows<Auth0ManagementServiceException> {
            auth0ManagementService.getUserByPhoneNumber(TEST_PHONE_NUMBER)
        }
    }

    @Test
    fun testGetUserByEmail_ManagementApiReturnsUser_ReturnFirstUser() {
        val expectedUserList = listOf(createSampleAuth0User())
        val request = mockk<Request<List<User>>>()

        every {
            request.execute()
        }.returns(expectedUserList)

        every {
            managementApi.users().listByEmail(TEST_EMAIL, null)
        }.returns(request)

        val actualUser = auth0ManagementService.getUserByEmail(TEST_EMAIL)

        assertNotNull(actualUser)
        assertEquals(expectedUserList[0], actualUser)
    }

    @Test
    fun testGetUserByEmail_ManagementApiReturnsNoUsers_ReturnNull() {
        val expectedUserList = emptyList<User>()
        val request = mockk<Request<List<User>>>()

        every {
            request.execute()
        }.returns(expectedUserList)

        every {
            managementApi.users().listByEmail(TEST_EMAIL, null)
        }.returns(request)

        val actualUser = auth0ManagementService.getUserByEmail(TEST_EMAIL)

        assertNull(actualUser)
    }

    @Test
    fun testGetUserByEmail_CallingAuth0ThrowsApiException_ThrowApiException() {
        val request = mockk<Request<List<User>>>()

        every {
            request.execute()
        }.throws(APIException("Something broke", 500, RuntimeException("Something broke")))

        every {
            managementApi.users().listByEmail(TEST_EMAIL, null)
        }.returns(request)

        assertThrows<APIException> {
            auth0ManagementService.getUserByEmail(TEST_EMAIL)
        }
    }

    @Test
    fun testGetUserByEmail_CallingAuth0ThrowsAuth0Exception_ThrowServiceException() {
        val request = mockk<Request<List<User>>>()

        every {
            request.execute()
        }.throws(Auth0Exception("something broke"))

        every {
            managementApi.users().listByEmail(TEST_EMAIL, null)
        }.returns(request)

        assertThrows<Auth0ManagementServiceException> {
            auth0ManagementService.getUserByEmail(TEST_EMAIL)
        }
    }

    @Test
    fun testUnblockUser_SuccessfullyUnblockUser_ReturnUser() {
        val expectedUser = createSampleAuth0User(isBlocked = false)
        val request = mockk<Request<User>>()

        every {
            request.execute()
        }.returns(expectedUser)

        every {
            managementApi.users().update(expectedUser.id, any())
        }.returns(request)

        val actualUser = auth0ManagementService.unblockUser(expectedUser.id)

        assertUser(expectedUser, actualUser)
    }

    @Test
    fun testUnblockUser_CallingAuth0ThrowsAPIException_ReturnAPIException() {
        val expectedUser = createSampleAuth0User(isBlocked = false)
        val request = mockk<Request<User>>()

        every {
            request.execute()
        }.throws(APIException("Failed to update user", 400, RuntimeException("Failed to update user")))

        every {
            managementApi.users().update(any(), any())
        }.returns(request)

        assertThrows<APIException> {
            auth0ManagementService.unblockUser(expectedUser.id)
        }
    }

    @Test
    fun testUnblockUser_CallingAuth0ThrowsAuth0Exception_ReturnAPIException() {
        val expectedUser = createSampleAuth0User(isBlocked = false)
        val request = mockk<Request<User>>()

        every {
            request.execute()
        }.throws(Auth0Exception("Something broke"))

        every {
            managementApi.users().update(any(), any())
        }.returns(request)

        assertThrows<Auth0ManagementServiceException> {
            auth0ManagementService.unblockUser(expectedUser.id)
        }
    }

    @Test
    fun testUpdateUserContactInfo_SuccessfullyUpdateUserContactInfo_ReturnUser() {
        val expectedUser = createSampleAuth0User()
        val request = mockk<Request<User>>()

        every {
            request.execute()
        }.returns(expectedUser)

        every {
            managementApi.users().update(expectedUser.id, any())
        }.returns(request)

        val actualUser = auth0ManagementService.updateUserPhoneNumber(expectedUser.id, expectedUser.phoneNumber)

        assertUser(expectedUser, actualUser)
    }

    @Test
    fun testUpdateUserContactInfo_CallingAuth0ThrowsAPIException_ReturnAPIException() {
        val expectedUser = createSampleAuth0User()
        val request = mockk<Request<User>>()

        every {
            request.execute()
        }.throws(APIException("Failed to update user", 400, RuntimeException("Failed to update user")))

        every {
            managementApi.users().update(expectedUser.id, any())
        }.returns(request)

        assertThrows<APIException> {
            auth0ManagementService.updateUserPhoneNumber(expectedUser.id, expectedUser.phoneNumber)
        }
    }

    @Test
    fun testUpdateUserContactInfo_CallingAuth0ThrowsAuth0Exception_ReturnAPIException() {
        val expectedUser = createSampleAuth0User()
        val request = mockk<Request<User>>()

        every {
            request.execute()
        }.throws(Auth0Exception("Something broke"))

        every {
            managementApi.users().update(expectedUser.id, any())
        }.returns(request)

        assertThrows<Auth0ManagementServiceException> {
            auth0ManagementService.updateUserPhoneNumber(expectedUser.id, expectedUser.phoneNumber)
        }
    }

    @Test
    fun testDeleteUser_SuccessfullyDeleteUser_ReturnVoid() {
        val expectedUser = createSampleAuth0User(isBlocked = false)
        val request = mockk<Request<Void>>()

        justRun {
            request.execute()
        }

        every {
            managementApi.users().delete(expectedUser.id)
        }.returns(request)

        auth0ManagementService.deleteUser(expectedUser.id)
    }

    @Test
    fun testDeleteUser_CallingAuth0ThrowsAPIException_ReturnAPIException() {
        val expectedUser = createSampleAuth0User(isBlocked = false)
        val request = mockk<Request<Void>>()

        every {
            request.execute()
        }.throws(APIException("Failed to update user", 400, RuntimeException("Failed to update user")))

        every {
            managementApi.users().delete(expectedUser.id)
        }.returns(request)

        assertThrows<APIException> {
            auth0ManagementService.deleteUser(expectedUser.id)
        }
    }

    @Test
    fun testDeleteUser_CallingAuth0ThrowsAuth0Exception_ReturnAPIException() {
        val expectedUser = createSampleAuth0User(isBlocked = false)
        val request = mockk<Request<Void>>()

        every {
            request.execute()
        }.throws(Auth0Exception("Something broke"))

        every {
            managementApi.users().delete(expectedUser.id)
        }.returns(request)

        assertThrows<Auth0ManagementServiceException> {
            auth0ManagementService.deleteUser(expectedUser.id)
        }
    }

    private fun createGetSecretValueResult(): GetSecretValueResult {
        val auth0ManagementApiToken = Auth0ManagementApiToken("token", "scope", 86400, "Bearer")
        val getSecretResult = GetSecretValueResult()
        getSecretResult.secretString = Gson().toJson(auth0ManagementApiToken)

        return getSecretResult
    }

    private fun assertUser(expected: User, actual: User) {
        assertEquals(expected.name, actual.name)
        assertEquals(expected.phoneNumber, actual.phoneNumber)
        assertEquals(expected.isPhoneVerified, actual.isPhoneVerified)
        assertEquals(TEST_CLINICIAN_AUTH0_USER_ID, actual.id)
    }
}