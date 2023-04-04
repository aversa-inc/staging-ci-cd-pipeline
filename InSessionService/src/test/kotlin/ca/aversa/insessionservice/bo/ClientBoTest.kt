package ca.aversa.insessionservice.bo

import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.exception.DuplicateResourceException
import ca.aversa.insessionservice.exception.EmailServiceException
import ca.aversa.insessionservice.exception.RegisterUserException
import ca.aversa.insessionservice.model.Client
import ca.aversa.insessionservice.model.entity.ClientTableRow
import ca.aversa.insessionservice.service.EmailService
import ca.aversa.insessionservice.util.EmailUtils
import ca.aversa.insessionservice.util.TestDefaults
import ca.aversa.insessionservice.util.TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID
import ca.aversa.insessionservice.util.UserTestUtils
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ClientBoTest {

    private val userBo: UserBo<Client, ClientTableRow> = mockk()
    private val clientDao: BasicDao<ClientTableRow> = mockk()
    private val emailService: EmailService = mockk()
    private val clientBo: ClientBo = ClientBo(userBo, emailService, clientDao)
    private val client = UserTestUtils.createSampleClient()
    private val auth0UserId = "auth0user"
    private val clinicianId = TEST_CLINICIAN_AUTH0_USER_ID

    @Test
    fun testUpdate_UpdateClientAttributes_NoExceptionThrown() {
        every {
            userBo.updateProfile(client, auth0UserId)
        }.returns(client)

        clientBo.updateProfile(client, auth0UserId)
    }

    @Test
    fun testUpdate_UserBoThrowsException_ThrowClientBoException() {
        every {
            userBo.updateProfile(any(), any())
        }.throws(Exception("Something broke"))

        assertThrows<Exception> {
            clientBo.updateProfile(client, auth0UserId)
        }
    }

    @Test
    fun testRegister_UserDoesNotExist_AddUserAndReturnTrue() {
        justRun {
            userBo.register(client, auth0UserId)
        }

        justRun {
            emailService.sendEmail(any(), any(), any())
        }

        clientBo.register(client, auth0UserId, clinicianId)

        verify {
            emailService.sendEmail(
                client.email,
                EmailUtils.CLIENT_INVITATION_ACCEPT_SUBJECT,
                EmailUtils.invitationApprovalEmail("${client.firstName} ${client.lastName}")
            )
        }
    }

    @Test
    fun testRegister_UserDoesExists_DontUpdateUserAndThrowDuplicateResourceException() {
        every {
            userBo.register(client, auth0UserId)
        }.throws(DuplicateResourceException("Duplicate user"))

        assertThrows<DuplicateResourceException> {
            clientBo.register(client, auth0UserId, clinicianId)
        }
    }

    @Test
    fun testRegister_UserBoThrowsException_ThrowRegisterUserException() {
        every {
            userBo.register(client, auth0UserId)
        }.throws(RuntimeException("kjsdhfjhksdjkf"))

        assertThrows<RegisterUserException> {
            clientBo.register(client, auth0UserId, clinicianId)
        }
    }

    @Test
    fun testRegister_EmailServiceThrowsException_ThrowRegisterUserException() {
        justRun {
            userBo.register(client, auth0UserId)
        }

        every {
            emailService.sendEmail(any(), any(), any())
        }.throws(EmailServiceException("something broke while sending email"))

        assertThrows<RegisterUserException> {
            clientBo.register(client, auth0UserId, clinicianId)
        }
    }

    @Test
    fun testGetProfiles_ProfilesExistForGivenIds_ReturnProfilesList() {
        val clientTableRows = listOf(client.toEntity(auth0UserId))
        val clientIds = listOf(auth0UserId)
        val expectedClients = listOf(client)

        every {
            clientDao.batchLoad(clientIds)
        }.returns(clientTableRows)

        val actualClients = clientBo.getProfiles(clientIds)

        assertEquals(expectedClients, actualClients)
    }

    @Test
    fun testGetProfiles_ProfilesDoNotExistForGivenIds_ReturnEmptyList() {
        val clientIds = listOf(auth0UserId)

        every {
            clientDao.batchLoad(clientIds)
        }.returns(emptyList())

        val actualClients = clientBo.getProfiles(clientIds)

        assertTrue(actualClients.isEmpty())
    }

    @Test
    fun testUpdateFcmToken_CallUserBoToUpdateToken_NoExceptionThrown() {
        justRun {
            userBo.updateFcmToken(auth0UserId, TestDefaults.TEST_GCM_TOKEN)
        }

        clientBo.updateFcmToken(auth0UserId, TestDefaults.TEST_GCM_TOKEN)
    }
}