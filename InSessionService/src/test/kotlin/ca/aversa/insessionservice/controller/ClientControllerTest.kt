package ca.aversa.insessionservice.controller

import ca.aversa.insessionservice.bo.ClientBo
import ca.aversa.insessionservice.bo.ClinicianBo
import ca.aversa.insessionservice.context.RequestAttributeType
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.exception.UserProfileUpdateException
import ca.aversa.insessionservice.model.Client
import ca.aversa.insessionservice.util.TestDefaults
import ca.aversa.insessionservice.util.UserTestUtils
import ca.aversa.insessionservice.util.UserTestUtils.createSampleClient
import ca.aversa.insessionservice.util.UserTestUtils.createSampleClinician
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import javax.servlet.http.HttpServletRequest

internal class ClientControllerTest {

    private val httpServletRequest: HttpServletRequest = mockk()
    private val clientBo: ClientBo = mockk()
    private val clinicianBo: ClinicianBo = mockk()
    private val clientController: ClientController = ClientController(clientBo, clinicianBo)

    @BeforeEach
    fun setup() {
        every {
            httpServletRequest.getAttribute(RequestAttributeType.CONTEXT.toString())
        }.returns(TestDefaults.TEST_CLIENT_CONTEXT)
    }

    @Test
    fun testUpdateProfile_CallClinicianBoToUpdateProfile_ReturnSuccessfulResponse() {
        val request = UserTestUtils.createSampleClientUpdateProfileRequest()
        val client = Client.Mapper.from(request)

        every {
            clientBo.updateProfile(client, eq(TestDefaults.TEST_CLIENT_CONTEXT.auth0UserId))
        }.returns(client)

        val response = clientController.updateProfile(httpServletRequest, request)

        assertEquals(response.statusCode, HttpStatus.ACCEPTED)
        assertEquals(client, response.body!!.user)
    }

    @Test
    fun testUpdateProfile_ClinicianBoThrowsExceptionWithFieldErrors_ReturnFieldErrorsResponse() {
        val request = UserTestUtils.createSampleClientUpdateProfileRequest()
        val fieldErrors = mapOf(
            "email" to "incorrect email",
            "phone" to "incorrect phone"
        )

        every {
            clientBo.updateProfile(any(), any())
        }.throws(UserProfileUpdateException(fieldErrors = fieldErrors))

        val response = clientController.updateProfile(httpServletRequest, request)

        assertEquals(response.statusCode, HttpStatus.CONFLICT)
        assertEquals(response.body!!.fieldErrors, fieldErrors)
    }

    @Test
    fun testGetClinicianProfile_ClientAndItsClinicianFound_ReturnClinicianProfile() {
        val client = createSampleClient()
        val expectedClinician = createSampleClinician()
        val expectedClients = listOf(client)

        every {
            clientBo.getProfiles(listOf(client.auth0UserId!!))
        }.returns(expectedClients)

        every {
            clinicianBo.getProfile(client.clinicianId)
        }.returns(expectedClinician)

        val response = clientController.getClinicianProfile(httpServletRequest)

        assertEquals(expectedClinician.firstName, response.firstName)
        assertEquals(expectedClinician.lastName, response.lastName)
        assertEquals(expectedClinician.type, response.type)
    }

    @Test
    fun testGetClinicianProfile_ClientNotFound_ThrowResourceNotFoundException() {
        every {
            clientBo.getProfiles(any())
        }.returns(emptyList())

        assertThrows<ResourceNotFoundException> {
            clientController.getClinicianProfile(httpServletRequest)
        }
    }

    @Test
    fun testGetClinicianProfile_ClinicanNotFound_ThrowResourceNotFoundException() {
        val client = createSampleClient()
        val expectedClients = listOf(client)

        every {
            clientBo.getProfiles(listOf(client.auth0UserId!!))
        }.returns(expectedClients)

        every {
            clinicianBo.getProfile(client.clinicianId)
        }.throws(ResourceNotFoundException("Clinician not found"))

        assertThrows<ResourceNotFoundException> {
            clientController.getClinicianProfile(httpServletRequest)
        }
    }

    @Test
    fun testProfile_ClientFound_ReturnClientProfile() {
        val expectedClient = createSampleClient()

        every {
            clientBo.getProfiles(listOf(expectedClient.auth0UserId!!))
        }.returns(listOf(expectedClient))

        val actualClient = clientController.getProfile(httpServletRequest)

        assertEquals(expectedClient, actualClient)
    }

    @Test
    fun testProfile_ClientProfileNotFound_ThrowResourceNotFoundException() {
        every {
            clientBo.getProfiles(listOf(TestDefaults.TEST_CLIENT_AUTH0_USER_ID))
        }.throws(ResourceNotFoundException("Client not found"));

        assertThrows<ResourceNotFoundException> {
            clientController.getProfile(httpServletRequest)
        }
    }
}