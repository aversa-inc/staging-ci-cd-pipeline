package ca.aversa.insessionservice.bo

import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.exception.ClinicianBoException
import ca.aversa.insessionservice.exception.DuplicateResourceException
import ca.aversa.insessionservice.exception.EmailServiceException
import ca.aversa.insessionservice.exception.RegisterUserException
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.model.Clinician
import ca.aversa.insessionservice.model.Day
import ca.aversa.insessionservice.model.Group
import ca.aversa.insessionservice.model.entity.ClinicianTableRow
import ca.aversa.insessionservice.model.entity.UserGroupTableRow
import ca.aversa.insessionservice.service.EmailService
import ca.aversa.insessionservice.util.EmailUtils
import ca.aversa.insessionservice.util.TestDefaults
import ca.aversa.insessionservice.util.UserTestUtils.createSampleBusinessProfile
import ca.aversa.insessionservice.util.UserTestUtils.createSampleClinician
import ca.aversa.insessionservice.util.UserTestUtils.createSampleClinicianAvailability
import ca.aversa.insessionservice.util.UserTestUtils.createSampleGroup
import ca.aversa.insessionservice.util.Utils
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ClinicianBoTest {

    private val userBo: UserBo<Clinician, ClinicianTableRow> = mockk()
    private val clinicianDao: BasicDao<ClinicianTableRow> = mockk()
    private val groupDao: BasicDao<UserGroupTableRow> = mockk()
    private val emailService: EmailService = mockk()
    private val clinicianBo: ClinicianBo = ClinicianBo(userBo, clinicianDao, groupDao, emailService)
    private val clinician = createSampleClinician()
    private val auth0UserId = "auth0user"
    private val managedClientUserId = "clientAuth0userid"
    private val groupId = "groupId"

    @BeforeEach
    fun setup() {
        mockkObject(Utils)

        every {
            Utils.createSecureId()
        }.returns(groupId)
    }

    @Test
    fun testUpdate_UpdateClinicianAttributes_NoExceptionThrown() {
        every {
            userBo.updateProfile(clinician, auth0UserId)
        }.returns(clinician)

        clinicianBo.updateProfile(clinician, auth0UserId)
    }

    @Test
    fun testUpdate_UserBoThrowsException_ThrowClientBoException() {
        every {
            userBo.updateProfile(any(), any())
        }.throws(Exception("Something broke"))

        assertThrows<Exception> {
            clinicianBo.updateProfile(clinician, auth0UserId)
        }
    }

    @Test
    fun testRegister_UserDoesNotExist_AddUserAndReturnTrue() {
        justRun {
            userBo.register(clinician, auth0UserId)
        }

        justRun {
            emailService.sendEmail(any(), any(), any())
        }

        clinicianBo.register(clinician, auth0UserId)

        verify {
            emailService.sendEmail(
                clinician.email,
                EmailUtils.CLINICIAN_INVITATION_APPROVAL_SUBJECT,
                EmailUtils.invitationApprovalEmail("${clinician.firstName} ${clinician.lastName}")
            )
        }
    }

    @Test
    fun testRegister_UserDoesExists_DontUpdateUserAndThrowDuplicateResourceException() {
        every {
            userBo.register(clinician, auth0UserId)
        }.throws(DuplicateResourceException("Duplicate user"))

        assertThrows<DuplicateResourceException> {
            clinicianBo.register(clinician, auth0UserId)
        }
    }

    @Test
    fun testRegister_DynamoDbGetThrowsException_ThrowRegisterUserException() {
        every {
            userBo.register(clinician, auth0UserId)
        }.throws(RuntimeException("kjsdhfjhksdjkf"))

        assertThrows<RegisterUserException> {
            clinicianBo.register(clinician, auth0UserId)
        }

        verify(exactly = 0) {
            emailService.sendEmail(
                clinician.email,
                EmailUtils.CLINICIAN_INVITATION_APPROVAL_SUBJECT,
                EmailUtils.invitationApprovalEmail("${clinician.firstName} ${clinician.lastName}")
            )
        }
    }

    @Test
    fun testRegister_EmailServiceThrowsException_ThrowRegisterUserException() {
        justRun {
            userBo.register(clinician, auth0UserId)
        }

        every {
            emailService.sendEmail(any(), any(), any())
        }.throws(EmailServiceException("something broke while sending email"))

        assertThrows<RegisterUserException> {
            clinicianBo.register(clinician, auth0UserId)
        }
    }

    @Test
    fun testAddManagedClient_ValidClinicianGivenToAddClientFor_Successful() {
        val clinicianTableRow = clinician.toEntity(auth0UserId)

        every {
            clinicianDao.get(auth0UserId)
        }.returns(clinicianTableRow)

        clinicianTableRow.clients.add(managedClientUserId)

        justRun {
            clinicianDao.update(clinicianTableRow)
        }

        clinicianBo.addManagedClient(auth0UserId, managedClientUserId)
    }

    @Test
    fun testAddManagedClient_ClinicianNotFound_ThrowResourceNotFoundException() {
        every {
            clinicianDao.get(auth0UserId)
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            clinicianBo.addManagedClient(auth0UserId, managedClientUserId)
        }
    }

    @Test
    fun testGetManagedClients_ClinicianHasClients_ReturnListOfClientIds() {
        val clinicianTableRow = clinician.toEntity(auth0UserId)
        clinicianTableRow.clients.add(managedClientUserId)

        every {
            clinicianDao.get(auth0UserId)
        }.returns(clinicianTableRow)

        val actualClientIds = clinicianBo.getManagedClients(auth0UserId)

        assertEquals(clinicianTableRow.clients, actualClientIds)
    }

    @Test
    fun testGetProfile_ClinicianExists_ReturnClinicianProfile() {
        val clinicianTableRow = clinician.toEntity(auth0UserId)

        every {
            clinicianDao.get(auth0UserId)
        }.returns(clinicianTableRow)

        val actualClinician = clinicianBo.getProfile(auth0UserId)

        assertEquals(Clinician.Mapper.from(clinicianTableRow), actualClinician)
    }

    @Test
    fun testGetProfile_ClinicianDoesNotExist_ThrowResourceNotFoundException() {
        every {
            clinicianDao.get(auth0UserId)
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            clinicianBo.getProfile(auth0UserId)
        }
    }

    @Test
    fun testUpdateFcmToken_CallUserBoToUpdateToken_NoExceptionThrown() {
        justRun {
            userBo.updateFcmToken(auth0UserId, TestDefaults.TEST_GCM_TOKEN)
        }

        clinicianBo.updateFcmToken(auth0UserId, TestDefaults.TEST_GCM_TOKEN)
    }

    @Test
    fun testUpdateAvailability_ValidClinicianToUpdateAvailabilityFor_Success() {
        val availability = createSampleClinicianAvailability()
        val clinicianTableRow = clinician.toEntity(auth0UserId)

        every {
            clinicianDao.get(auth0UserId)
        }.returns(clinicianTableRow)

        clinicianTableRow.availabilities = availability.toEntityAvailabilities()

        justRun {
            clinicianDao.update(clinicianTableRow)
        }

        clinicianBo.updateAvailability(auth0UserId, availability)
    }

    @Test
    fun testUpdateAvailability_ClinicianNotFound_ThrowResourceNotFoundException() {
        val availability = createSampleClinicianAvailability()

        every {
            clinicianDao.get(auth0UserId)
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            clinicianBo.updateAvailability(auth0UserId, availability)
        }
    }

    @Test
    fun testGetAvailabilities_ClinicianFound_ReturnAvailabilities() {
        val expectedAvailabilities = createSampleClinicianAvailability().toEntityAvailabilities()
        val clinicianTableRow = clinician.toEntity(auth0UserId)
        clinicianTableRow.availabilities = expectedAvailabilities

        every {
            clinicianDao.get(auth0UserId)
        }.returns(clinicianTableRow)

        val actualClinicianAvailability = clinicianBo.getAvailability(auth0UserId)
        val actualAvailabilities = actualClinicianAvailability.availabilities

        assertEquals(expectedAvailabilities.keys.toList(), actualAvailabilities.keys.map(Day::name))
    }

    @Test
    fun testGetAvailabilities_ClinicianNotFound_ThrowResourceNotFoundException() {
        every {
            clinicianDao.get(auth0UserId)
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            clinicianBo.getAvailability(auth0UserId)
        }
    }

    @Test
    fun testCreateGroup_CreateGroupForTheGivenClinician_Successful() {
        val expectedGroup = createSampleGroup()

        justRun {
            groupDao.update(expectedGroup.toEntity(groupId))
        }

        clinicianBo.createGroup(expectedGroup)
    }

    @Test
    fun testCreateGroup_GroupDaoThrowsException_ThrowException() {
        val expectedGroup = createSampleGroup()

        every {
            groupDao.update(expectedGroup.toEntity(groupId))
        }.throws(RuntimeException("Something broke"))

        assertThrows<ClinicianBoException> {
            clinicianBo.createGroup(expectedGroup)
        }
    }

    @Test
    fun testGetGroups_DaoReturnsResults_ReturnParsedResults() {
        val expectedGroups = listOf(createSampleGroup().toEntity(groupId))
        val keyConditionExpression = "id = :id"
        val valuesMap = mutableMapOf<String, AttributeValue>(
            ":id" to AttributeValue().withS(auth0UserId)
        )

        every {
            groupDao.query(keyConditionExpression, valuesMap)
        }.returns(expectedGroups)

        val actualGroups = clinicianBo.getGroups(auth0UserId)

        assertEquals(expectedGroups.map(Group.Mapper::from), actualGroups)
    }

    @Test
    fun testGetGroups_DaoThrowsException_ThrowException() {
        val keyConditionExpression = "id = :id"
        val valuesMap = mutableMapOf<String, AttributeValue>(
            ":id" to AttributeValue().withS(auth0UserId)
        )

        every {
            groupDao.query(keyConditionExpression, valuesMap)
        }.throws(RuntimeException("Something broke"))

        assertThrows<ClinicianBoException> {
            clinicianBo.getGroups(auth0UserId)
        }
    }

    @Test
    fun testUpdateBusinessProfile_ClinicianFoundToUpdateProfileFor_Successful() {
        val expectedBusinessProfile = createSampleBusinessProfile()
        val clinicianToUpdate = createSampleClinician(businessProfile = expectedBusinessProfile)

        every {
            clinicianDao.get(auth0UserId)
        }.returns(clinician.toEntity(auth0UserId))

        justRun {
            clinicianDao.update(clinicianToUpdate.toEntity(auth0UserId))
        }

        clinicianBo.updateBusinessProfile(auth0UserId, expectedBusinessProfile)
    }

    @Test
    fun testUpdateBusinessProfile_ClinicianNotFound_ThrowResourceNotFoundException() {
        val expectedBusinessProfile = createSampleBusinessProfile()

        every {
            clinicianDao.get(auth0UserId)
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            clinicianBo.updateBusinessProfile(auth0UserId, expectedBusinessProfile)
        }
    }

    @Test
    fun testUpdateBusinessProfile_DynamoDbThrowsExceptionWhileFetchingClinician_ThrowClinicianBoException() {
        val expectedBusinessProfile = createSampleBusinessProfile()

        every {
            clinicianDao.get(auth0UserId)
        }.throws(RuntimeException("Something broke"))

        assertThrows<ClinicianBoException> {
            clinicianBo.updateBusinessProfile(auth0UserId, expectedBusinessProfile)
        }
    }

    @Test
    fun testUpdateBusinessProfile_DDBThrowsExceptionWhileUpdatingProfile_ThrowClinicianBoException() {
        val expectedBusinessProfile = createSampleBusinessProfile()
        val clinicianToUpdate = createSampleClinician(businessProfile = expectedBusinessProfile)

        every {
            clinicianDao.get(auth0UserId)
        }.returns(clinician.toEntity(auth0UserId))

        every {
            clinicianDao.update(clinicianToUpdate.toEntity(auth0UserId))
        }.throws(RuntimeException("Something broke"))

        assertThrows<ClinicianBoException> {
            clinicianBo.updateBusinessProfile(auth0UserId, expectedBusinessProfile)
        }
    }

    @Test
    fun testGetBusinessProfile_ClinicianExistsWithNonNullBusinessProfile_ReturnBusinessProfile() {
        val expectedBusinessProfile = createSampleBusinessProfile()
        val clinician = createSampleClinician(businessProfile = expectedBusinessProfile)

        every {
            clinicianDao.get(auth0UserId)
        }.returns(clinician.toEntity(auth0UserId))

        val actualBusinessProfile = clinicianBo.getBusinessProfile(auth0UserId)

        assertEquals(expectedBusinessProfile, actualBusinessProfile);
    }

    @Test
    fun testGetBusinessProfile_ClinicianExistsWithNullBusinessProfile_ReturnNull() {
        every {
            clinicianDao.get(auth0UserId)
        }.returns(clinician.toEntity(auth0UserId))

        val actualBusinessProfile = clinicianBo.getBusinessProfile(auth0UserId)

        assertNull(actualBusinessProfile)
    }

    @Test
    fun testGetBusinessProfile_ClinicianDoesntExist_ThrowResourceNotFoundException() {
        every {
            clinicianDao.get(auth0UserId)
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            clinicianBo.getBusinessProfile(auth0UserId)
        }
    }
}