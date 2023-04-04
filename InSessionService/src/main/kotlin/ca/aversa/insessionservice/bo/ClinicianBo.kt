package ca.aversa.insessionservice.bo

import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.exception.ClinicianBoException
import ca.aversa.insessionservice.exception.DuplicateResourceException
import ca.aversa.insessionservice.exception.RegisterUserException
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.model.Clinician
import ca.aversa.insessionservice.model.ClinicianAvailability
import ca.aversa.insessionservice.model.ClinicianBusinessProfile
import ca.aversa.insessionservice.model.Group
import ca.aversa.insessionservice.model.entity.ClinicianTableRow
import ca.aversa.insessionservice.model.entity.UserGroupTableRow
import ca.aversa.insessionservice.service.EmailService
import ca.aversa.insessionservice.util.EmailUtils
import ca.aversa.insessionservice.util.Utils
import com.amazonaws.services.dynamodbv2.model.AttributeValue

open class ClinicianBo(

    private val userBo: UserBo<Clinician, ClinicianTableRow>,
    private val clinicianDao: BasicDao<ClinicianTableRow>,
    private val groupDao: BasicDao<UserGroupTableRow>,
    private val emailService: EmailService
) {

    fun doesUserExist(email: String): Boolean {
        return userBo.doesUserExist(email)
    }

    /**
     * Register the given user by adding them to DDB, Auth0, and sending an invitation email.
     * If the user already exists, then throw DuplicateResourceException
     */
    fun register(user: Clinician, auth0UserId: String) {
        try {
            userBo.register(user, auth0UserId)

            emailService.sendEmail(
                user.email,
                EmailUtils.CLINICIAN_INVITATION_APPROVAL_SUBJECT,
                EmailUtils.invitationApprovalEmail("${user.firstName} ${user.lastName}")
            )
        }
        catch (e: DuplicateResourceException) {
            throw e
        }
        catch (e: Exception) {
            throw RegisterUserException(e)
        }
    }

    /**
     * Update clinician attributes in DynamoDB
     */
    fun updateProfile(user: Clinician, auth0UserId: String): Clinician {
        return userBo.updateProfile(user, auth0UserId)
    }

    fun getProfile(clinicianUserId: String): Clinician {
        val clinicianTableRow = getClinicianFromDynamo(clinicianUserId)

        return Clinician.Mapper.from(clinicianTableRow)
    }

    fun addManagedClient(clinicianUserId: String, clientUserId: String) {
        val clinicianTableRow = getClinicianFromDynamo(clinicianUserId)
        clinicianTableRow.clients.add(clientUserId)

        clinicianDao.update(clinicianTableRow)
    }

    fun getManagedClients(clinicianUserId: String): List<String> {
        val clinicianTableRow = getClinicianFromDynamo(clinicianUserId)

        return clinicianTableRow.clients
    }

    fun updateFcmToken(clinicianUserId: String, fcmToken: String) {
        userBo.updateFcmToken(clinicianUserId, fcmToken)
    }

    fun updateAvailability(clinicianUserId: String, availability: ClinicianAvailability) {
        val clinicianTableRow = getClinicianFromDynamo(clinicianUserId)
        clinicianTableRow.availabilities = availability.toEntityAvailabilities()

        clinicianDao.update(clinicianTableRow)
    }

    fun getAvailability(clinicianUserId: String): ClinicianAvailability {
        val clinicianTableRow = getClinicianFromDynamo(clinicianUserId)

        return ClinicianAvailability.Mapper.from(clinicianTableRow.availabilities)
    }

    fun createGroup(group: Group) {
        try {
            val groupId = Utils.createSecureId()
            val groupTableRow = group.toEntity(groupId)

            groupDao.update(groupTableRow)
        }
        catch (e: Exception) {
            throw ClinicianBoException(cause = e)
        }
    }

    fun getGroups(clinicianUserId: String): List<Group> {
        try {
            val keyConditionExpression = "id = :id"
            val valuesMap = mutableMapOf<String, AttributeValue>(
                ":id" to AttributeValue().withS(clinicianUserId)
            )

            val groupTableRows = groupDao.query(keyConditionExpression, valuesMap)

            return groupTableRows.map(Group.Mapper::from)
        }
        catch (e: Exception) {
            throw ClinicianBoException(cause = e)
        }
    }

    fun updateBusinessProfile(clinicianUserId: String, businessProfile: ClinicianBusinessProfile) {
        try {
            val clinicianTableRow = getClinicianFromDynamo(clinicianUserId)

            clinicianTableRow.businessProfile = businessProfile.toEntity()

            clinicianDao.update(clinicianTableRow)
        }
        catch (e: Exception) {
            when(e) {
                is ResourceNotFoundException -> throw e
                else -> throw ClinicianBoException(cause = e)
            }
        }
    }

    fun getBusinessProfile(clinicianUserId: String): ClinicianBusinessProfile? {
        try {
            val clinicianTableRow = getClinicianFromDynamo(clinicianUserId)

            return clinicianTableRow.businessProfile?.let(ClinicianBusinessProfile.Mapper::from)
        }
        catch (e: Exception) {
            when(e) {
                is ResourceNotFoundException -> throw e
                else -> throw ClinicianBoException(cause = e)
            }
        }
    }

    private fun getClinicianFromDynamo(clinicianUserId: String): ClinicianTableRow {
        return clinicianDao.get(clinicianUserId)
            ?: throw ResourceNotFoundException("Clinician $clinicianUserId not found")
    }
}