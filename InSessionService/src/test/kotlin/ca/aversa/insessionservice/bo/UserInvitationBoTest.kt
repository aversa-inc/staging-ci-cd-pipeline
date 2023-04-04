package ca.aversa.insessionservice.bo

import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.exception.Auth0ManagementServiceException
import ca.aversa.insessionservice.exception.ClientBoException
import ca.aversa.insessionservice.exception.ClinicianBoException
import ca.aversa.insessionservice.exception.DuplicateResourceException
import ca.aversa.insessionservice.exception.EmailServiceException
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.exception.UserInvitationBoException
import ca.aversa.insessionservice.model.Client
import ca.aversa.insessionservice.model.Clinician
import ca.aversa.insessionservice.model.UserRole
import ca.aversa.insessionservice.model.entity.UserInvitationTableRow
import ca.aversa.insessionservice.service.Auth0ManagementService
import ca.aversa.insessionservice.service.EmailService
import ca.aversa.insessionservice.util.Auth0TestUtils
import ca.aversa.insessionservice.util.EmailUtils
import ca.aversa.insessionservice.util.RegisterAndLoginTestUtils.createUserSampleUserInvitationTableRow
import ca.aversa.insessionservice.util.TestDefaults
import ca.aversa.insessionservice.util.UserTestUtils
import com.google.gson.Gson
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

internal class UserInvitationBoTest {

    private val siteUrl: String = "https://siteurl.com"
    private val referredBy = "somereferredbyid"
    private val clinicianBo: ClinicianBo = mockk()
    private val clientBo: ClientBo = mockk()
    private val userInvitationDao: BasicDao<UserInvitationTableRow> = mockk()
    private val auth0ManagementService: Auth0ManagementService = mockk()
    private val emailService: EmailService = mockk()
    private val gson: Gson = mockk()
    private val invitationBo: UserInvitationBo = UserInvitationBo(
        clinicianBo, clientBo, userInvitationDao, auth0ManagementService, emailService, gson, siteUrl
    )

    private var invitationApproveUrl = "$siteUrl/invitation/approve"
    private var invitationRejectionUrl = "$siteUrl/invitation/reject"
    private var uuid: String = ""

    @BeforeEach
    fun setup() {
        uuid = "uuid"

        mockkStatic(UUID::class)

        every {
            UUID.randomUUID().toString()
        }.returns(uuid)

        invitationApproveUrl += "/$uuid"
        invitationRejectionUrl += "/$uuid"
    }

    @Test
    fun testInvite_GivenUserIsClinician_AddInvitationToDynamoAndSendEmailToAdmin() {
        val clinician = UserTestUtils.createSampleClinician(availability = UserTestUtils.createSampleClinicianAvailability())
        val auth0User = Auth0TestUtils.createSampleAuth0UserAddResponse(false)

        every {
            auth0ManagementService.addUser(any())
        }.returns(auth0User)

        every {
            auth0ManagementService.getUserByPhoneNumber(clinician.phoneNumber)
        }.returns(null)

        every {
            clientBo.doesUserExist(clinician.email)
        }.returns(false)

        every {
            clinicianBo.doesUserExist(clinician.email)
        }.returns(false)

        every {
            gson.toJson(clinician)
        }.returns(TestDefaults.TEST_USER_INVITATION_USER_PAYLOAD)

        justRun {
            userInvitationDao.update(any())
        }

        justRun {
            emailService.sendEmail(any(), any(), any())
        }

        invitationBo.invite(clinician, referredBy)

        verify {
            auth0ManagementService.addUser(any())
            userInvitationDao.update(any())
            emailService.sendEmail(
                EmailUtils.ADMIN_EMAIL,
                EmailUtils.CLINICIAN_INVITATION_TO_ADMIN_SUBJECT,
                EmailUtils.getClinicianInvitationEmailForAdminBody(clinician,
                    invitationApproveUrl,
                    invitationRejectionUrl
                )
            )
        }
    }

    @Test
    fun testInvite_GivenUserIsClient_AddInvitationToDynamoAndSendEmailToClient() {
        val client = UserTestUtils.createSampleClient()
        val auth0User = Auth0TestUtils.createSampleAuth0UserAddResponse(false)

        every {
            auth0ManagementService.addUser(any())
        }.returns(auth0User)

        every {
            auth0ManagementService.getUserByPhoneNumber(client.phoneNumber)
        }.returns(null)

        every {
            auth0ManagementService.getUserByEmail(client.email)
        }.returns(null)

        every {
            clinicianBo.doesUserExist(client.email)
        }.returns(false)

        every {
            clientBo.doesUserExist(client.email)
        }.returns(false)

        every {
            gson.toJson(client)
        }.returns(TestDefaults.TEST_USER_INVITATION_USER_PAYLOAD)

        justRun {
            userInvitationDao.update(any())
        }

        justRun {
            emailService.sendEmail(any(), any(), any())
        }

        invitationBo.invite(client, referredBy)

        verify {
            auth0ManagementService.addUser(any())
            userInvitationDao.update(any())
            emailService.sendEmail(
                client.email,
                EmailUtils.CLIENT_INVITATION_SUBJECT,
                EmailUtils.getClientInvitationEmailBody(
                    client,
                    "$invitationApproveUrl?type=client",
                    invitationRejectionUrl
                )
            )
        }
    }

    @Test
    fun testInvite_GivenUserIsClinicianAndExistsInDynamoDb_ThrowDuplicateResourceException() {
        val clinician = UserTestUtils.createSampleClinician()

        every {
            clinicianBo.doesUserExist(clinician.email)
        }.returns(true)

        assertThrows<DuplicateResourceException> {
            invitationBo.invite(clinician, referredBy)
        }
    }

    @Test
    fun testInvite_GivenUserIsClientAndExistsInDynamoDb_ThrowDuplicateResourceException() {
        val client = UserTestUtils.createSampleClient()

        every {
            clinicianBo.doesUserExist(client.email)
        }.returns(true)

        every {
            clientBo.doesUserExist(client.email)
        }.returns(true)

        assertThrows<DuplicateResourceException> {
            invitationBo.invite(client, referredBy)
        }
    }

    @Test
    fun testInvite_Auth0ThrowsException_ThrowUserInvitationBoException() {
        val clinician = UserTestUtils.createSampleClinician()

        every {
            auth0ManagementService.addUser(any())
        }.throws(RuntimeException("something broke"))

        justRun {
            userInvitationDao.update(any())
        }

        justRun {
            emailService.sendEmail(any(), any(), any())
        }

        assertThrows<UserInvitationBoException> {
            invitationBo.invite(clinician, referredBy)
        }
    }

    @Test
    fun testInvite_DynamoDbThrowsException_ThrowUserInvitationBoException() {
        val clinician = UserTestUtils.createSampleClinician()
        val auth0User = Auth0TestUtils.createSampleAuth0UserAddResponse(false)

        every {
            auth0ManagementService.addUser(any())
        }.returns(auth0User)

        every {
            userInvitationDao.update(any())
        }.throws(Auth0ManagementServiceException("Something broke"))

        justRun {
            emailService.sendEmail(any(), any(), any())
        }

        assertThrows<UserInvitationBoException> {
            invitationBo.invite(clinician, referredBy)
        }
    }

    @Test
    fun testInvite_EmailServiceThrowsException_ThrowUserInvitationBoException() {
        val clinician = UserTestUtils.createSampleClinician()
        val auth0User = Auth0TestUtils.createSampleAuth0UserAddResponse(false)

        every {
            auth0ManagementService.addUser(any())
        }.returns(auth0User)

        justRun {
            userInvitationDao.update(any())
        }

        every {
            emailService.sendEmail(any(), any(), any())
        }.throws(EmailServiceException("Something broke"))

        assertThrows<UserInvitationBoException> {
            invitationBo.invite(clinician, referredBy)
        }
    }

    @Test
    fun testApprove_ApproveValidClinicianInvitation_ReturnTrue() {
        val clinician = UserTestUtils.createSampleClinician()
        val expectedUserInvitationTableRow = createUserSampleUserInvitationTableRow(userRole = UserRole.CLINICIAN)

        every {
            userInvitationDao.get(any())
        }.returns(expectedUserInvitationTableRow)

        every {
            gson.fromJson(any<String>(), Clinician::class.java)
        }.returns(clinician)

        justRun {
            clinicianBo.register(any(), any())
        }

        every {
            auth0ManagementService.unblockUser(any())
        }.returns(clinician.toAuth0User())

        justRun {
            userInvitationDao.delete(expectedUserInvitationTableRow)
        }

        invitationBo.approve(expectedUserInvitationTableRow.id)

        verify {
            userInvitationDao.get(expectedUserInvitationTableRow.id)
            gson.fromJson(expectedUserInvitationTableRow.userPayload, Clinician::class.java)
            clinicianBo.register(clinician, expectedUserInvitationTableRow.auth0UserId)
            auth0ManagementService.unblockUser(expectedUserInvitationTableRow.auth0UserId)
            userInvitationDao.delete(expectedUserInvitationTableRow)
        }
    }

    @Test
    fun testApprove_ApproveValidClientInvitation_ReturnTrue() {
        val client = UserTestUtils.createSampleClient()
        val expectedUserInvitationTableRow = createUserSampleUserInvitationTableRow(userRole = UserRole.CLIENT)

        every {
            userInvitationDao.get(expectedUserInvitationTableRow.id)
        }.returns(expectedUserInvitationTableRow)

        every {
            gson.fromJson(any<String>(), Client::class.java)
        }.returns(client)

        every {
            auth0ManagementService.unblockUser(expectedUserInvitationTableRow.auth0UserId)
        }.returns(client.toAuth0User())

        justRun {
            clientBo.register(client, expectedUserInvitationTableRow.auth0UserId, expectedUserInvitationTableRow.referredBy)
        }

        justRun {
            userInvitationDao.delete(expectedUserInvitationTableRow)
        }

        justRun {
            clinicianBo.addManagedClient(expectedUserInvitationTableRow.referredBy, expectedUserInvitationTableRow.auth0UserId)
        }

        invitationBo.approve(expectedUserInvitationTableRow.id)
    }

    @Test
    fun testApprove_InvitationNotFound_ThrowResourceNotFoundException() {
        val expectedUserInvitationTableRow = createUserSampleUserInvitationTableRow(userRole = UserRole.CLINICIAN)

        every {
            userInvitationDao.get(any())
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            invitationBo.approve(expectedUserInvitationTableRow.id)
        }
    }

    @Test
    fun testApprove_DynamoDbThrowsException_ThrowUserInvitationException() {
        val expectedUserInvitationTableRow = createUserSampleUserInvitationTableRow(userRole = UserRole.CLINICIAN)

        every {
            userInvitationDao.get(any())
        }.throws(RuntimeException("something broke"))

        assertThrows<UserInvitationBoException> {
            invitationBo.approve(expectedUserInvitationTableRow.id)
        }
    }

    @Test
    fun testApprove_ClinicianBoThrowsException_ThrowUserInvitationException() {
        val clinician = UserTestUtils.createSampleClinician()
        val expectedUserInvitationTableRow = createUserSampleUserInvitationTableRow(userRole = UserRole.CLINICIAN)

        every {
            userInvitationDao.get(any())
        }.returns(expectedUserInvitationTableRow)

        every {
            gson.fromJson(any<String>(), Clinician::class.java)
        }.returns(clinician)

        every {
            clinicianBo.register(any(), any())
        }.throws(ClinicianBoException("something broke"))

        assertThrows<UserInvitationBoException> {
            invitationBo.approve(expectedUserInvitationTableRow.id)
        }
    }

    @Test
    fun testApprove_ClientBoThrowsException_ThrowUserInvitationException() {
        val client = UserTestUtils.createSampleClient()
        val expectedUserInvitationTableRow = createUserSampleUserInvitationTableRow(userRole = UserRole.CLIENT)

        every {
            userInvitationDao.get(any())
        }.returns(expectedUserInvitationTableRow)

        every {
            gson.fromJson(any<String>(), Client::class.java)
        }.returns(client)

        every {
            clientBo.register(any(), any(), any())
        }.throws(ClientBoException("something broke"))

        assertThrows<UserInvitationBoException> {
            invitationBo.approve(expectedUserInvitationTableRow.id)
        }
    }

    @Test
    fun testApprove_ClinicianBoThrowsDuplicateResourceException_ThrowDuplicateResourceException() {
        val clinician = UserTestUtils.createSampleClinician()
        val expectedUserInvitationTableRow = createUserSampleUserInvitationTableRow(userRole = UserRole.CLINICIAN)

        every {
            userInvitationDao.get(any())
        }.returns(expectedUserInvitationTableRow)

        every {
            gson.fromJson(any<String>(), Clinician::class.java)
        }.returns(clinician)

        every {
            clinicianBo.register(any(), any())
        }.throws(DuplicateResourceException("something broke"))

        assertThrows<DuplicateResourceException> {
            invitationBo.approve(expectedUserInvitationTableRow.id)
        }
    }

    @Test
    fun testApprove_ClientBoThrowsDuplicateResourceException_ThrowDuplicateResourceException() {
        val client = UserTestUtils.createSampleClient()
        val expectedUserInvitationTableRow = createUserSampleUserInvitationTableRow(userRole = UserRole.CLIENT)

        every {
            userInvitationDao.get(any())
        }.returns(expectedUserInvitationTableRow)

        every {
            gson.fromJson(any<String>(), Client::class.java)
        }.returns(client)

        every {
            clientBo.register(any(), any(), any())
        }.throws(DuplicateResourceException("something broke"))

        assertThrows<DuplicateResourceException> {
            invitationBo.approve(expectedUserInvitationTableRow.id)
        }
    }

    @Test
    fun testApprove_DynamoDbUpdateThrowsException_ThrowUserInvitationException() {
        val client = UserTestUtils.createSampleClient()
        val expectedUserInvitationTableRow = createUserSampleUserInvitationTableRow(userRole = UserRole.CLIENT)

        every {
            userInvitationDao.get(any())
        }.returns(expectedUserInvitationTableRow)

        every {
            gson.fromJson(any<String>(), Client::class.java)
        }.returns(client)

        every {
            clientBo.register(any(), any(), any())
        }.throws(ClientBoException("something broke"))

        every {
            userInvitationDao.update(any())
        }.throws(RuntimeException("it broke"))

        assertThrows<UserInvitationBoException> {
            invitationBo.approve(expectedUserInvitationTableRow.id)
        }
    }

    @Test
    fun testReject_InvitationFound_DeleteFromDynamoDbAndAuth0() {
        val clinician = UserTestUtils.createSampleClinician()
        val expectedUserInvitationTableRow = createUserSampleUserInvitationTableRow(userRole = UserRole.CLINICIAN)

        every {
            userInvitationDao.get(any())
        }.returns(expectedUserInvitationTableRow)

        every {
            gson.fromJson(any<String>(), Clinician::class.java)
        }.returns(clinician)

        every {
            clinicianBo.doesUserExist(clinician.email)
        }.returns(false)

        every {
            clientBo.doesUserExist(clinician.email)
        }.returns(false)

        every {
            auth0ManagementService.getUserByPhoneNumber(clinician.phoneNumber)
        }.returns(clinician.toAuth0User())

        justRun {
            userInvitationDao.delete(any())
        }

        justRun {
            auth0ManagementService.deleteUser(expectedUserInvitationTableRow.auth0UserId)
        }

        justRun {
            emailService.sendEmail(any(), any(), any())
        }

        invitationBo.reject(expectedUserInvitationTableRow.id)

        verify {
            userInvitationDao.get(expectedUserInvitationTableRow.id)
            gson.fromJson(expectedUserInvitationTableRow.userPayload, Clinician::class.java)
            userInvitationDao.delete(expectedUserInvitationTableRow)
            auth0ManagementService.deleteUser(expectedUserInvitationTableRow.auth0UserId)
            emailService.sendEmail(
                EmailUtils.ADMIN_EMAIL,
                EmailUtils.CLINICIAN_INVITATION_REJECTION_SUBJECT,
                EmailUtils.getClinicianInvitationRejectionEmail("${clinician.firstName} ${clinician.lastName}")
            )
        }
    }

    @Test
    fun testReject_InvitationNotFound_ThrowResourceNotFoundException() {
        val expectedUserInvitationTableRow = createUserSampleUserInvitationTableRow(userRole = UserRole.CLINICIAN)

        every {
            userInvitationDao.get(any())
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            invitationBo.reject(expectedUserInvitationTableRow.id)
        }
    }

    @Test
    fun testReject_InvitationFoundButUserAlreadyExists_DeleteFromInvitationTableAndThrowDuplicatException() {
        val clinician = UserTestUtils.createSampleClinician()
        val expectedUserInvitationTableRow = createUserSampleUserInvitationTableRow(userRole = UserRole.CLINICIAN)

        every {
            userInvitationDao.get(any())
        }.returns(expectedUserInvitationTableRow)

        every {
            gson.fromJson(any<String>(), Clinician::class.java)
        }.returns(clinician)

        every {
            clinicianBo.doesUserExist(clinician.email)
        }.returns(true)

        every {
            auth0ManagementService.getUserByPhoneNumber(clinician.phoneNumber)
        }.returns(clinician.toAuth0User())

        justRun {
            userInvitationDao.delete(expectedUserInvitationTableRow)
        }

        assertThrows<DuplicateResourceException> {
            invitationBo.reject(expectedUserInvitationTableRow.id)
        }
    }

    @Test
    fun testReject_DynamoDbGetThrowsException_ThrowUserInvitationBoException() {
        val expectedUserInvitationTableRow = createUserSampleUserInvitationTableRow(userRole = UserRole.CLINICIAN)

        every {
            userInvitationDao.get(any())
        }.throws(RuntimeException("invite not found"))

        assertThrows<UserInvitationBoException> {
            invitationBo.reject(expectedUserInvitationTableRow.id)
        }
    }

    @Test
    fun testReject_DynamoDbDeleteThrowsException_ThrowUserInvitationBoException() {
        val expectedUserInvitationTableRow = createUserSampleUserInvitationTableRow(userRole = UserRole.CLINICIAN)

        every {
            userInvitationDao.get(any())
        }.returns(expectedUserInvitationTableRow)

        justRun {
            userInvitationDao.delete(expectedUserInvitationTableRow)
        }

        assertThrows<UserInvitationBoException> {
            invitationBo.reject(expectedUserInvitationTableRow.id)
        }
    }

    @Test
    fun testReject_Auth0ThrowsAPIException_ThrowUserInvitationBoException() {
        val expectedUserInvitationTableRow = createUserSampleUserInvitationTableRow(userRole = UserRole.CLINICIAN)

        every {
            userInvitationDao.get(any())
        }.returns(expectedUserInvitationTableRow)

        justRun {
            userInvitationDao.delete(expectedUserInvitationTableRow)
        }

        every {
            auth0ManagementService.deleteUser(expectedUserInvitationTableRow.auth0UserId)
        }.throws(RuntimeException("it broke"))

        assertThrows<UserInvitationBoException> {
            invitationBo.reject(expectedUserInvitationTableRow.id)
        }
    }
}
