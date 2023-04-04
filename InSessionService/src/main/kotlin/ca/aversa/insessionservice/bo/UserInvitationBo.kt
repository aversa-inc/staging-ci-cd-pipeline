package ca.aversa.insessionservice.bo

import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.exception.ClientBoException
import ca.aversa.insessionservice.exception.ClinicianBoException
import ca.aversa.insessionservice.exception.DuplicateResourceException
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.exception.UserInvitationBoException
import ca.aversa.insessionservice.model.AbstractUser
import ca.aversa.insessionservice.model.Client
import ca.aversa.insessionservice.model.Clinician
import ca.aversa.insessionservice.model.UserRole
import ca.aversa.insessionservice.model.entity.UserInvitationTableRow
import ca.aversa.insessionservice.service.Auth0ManagementService
import ca.aversa.insessionservice.service.EmailService
import ca.aversa.insessionservice.util.EmailUtils
import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class UserInvitationBo(

    private val clinicianBo: ClinicianBo,
    private val clientBo: ClientBo,
    private val userInvitationDao: BasicDao<UserInvitationTableRow>,
    private val auth0ManagementService: Auth0ManagementService,
    private val emailService: EmailService,
    private val gson: Gson,
    siteUrl: String
) {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    private val invitationApproveUrl = "$siteUrl/invitation/approve"
    private val invitationRejectionUrl = "$siteUrl/invitation/reject"

    fun invite(user: AbstractUser, referredByAuth0UserId: String) {
        try {
            if(doesUserExistInDynamo(user)) {
                throw DuplicateResourceException("User with email ${user.email} or phone number already exists")
            }

            val uid = UUID.randomUUID().toString()

            val auth0User = auth0ManagementService.addUser(user.toAuth0User())

            val invitationTableRow = createInvitationTableRow(uid, auth0User.id, user, referredByAuth0UserId)
            userInvitationDao.update(invitationTableRow)

            sendInvitationEmail(user, uid)
        }
        catch (e: DuplicateResourceException) {
            throw e
        }
        catch (e: Exception) {
            log.error("Exception occurred while trying to invite user: ", e)

            throw UserInvitationBoException(cause = e)
        }
    }

    /**
     * Approve the given invitation. If the user already exists, the corresponding Bo will
     * throw DuplicateResource exception
     */
    fun approve(id: String) {
        try {
            val invitationTableRow = getInvitationTableRow(id)
            val user: AbstractUser

            log.info("Registering user with invitation: {}", invitationTableRow)

            if(invitationTableRow.userType == UserRole.CLINICIAN) {
                user = gson.fromJson(invitationTableRow.userPayload, Clinician::class.java)

                clinicianBo.register(user, invitationTableRow.auth0UserId)
            }
            else {
                user = gson.fromJson(invitationTableRow.userPayload, Client::class.java)

                clientBo.register(user, invitationTableRow.auth0UserId, invitationTableRow.referredBy)

                clinicianBo.addManagedClient(invitationTableRow.referredBy, invitationTableRow.auth0UserId)
            }

            userInvitationDao.delete(invitationTableRow)

            auth0ManagementService.unblockUser(invitationTableRow.auth0UserId)
        }
        catch (e: Exception) {
            when(e) {
                is ResourceNotFoundException, is DuplicateResourceException -> throw e
                is ClinicianBoException, is ClientBoException -> {
                    log.error("User Bo threw exception while registering user: {} - ", id, e)

                    throw UserInvitationBoException(cause = e)
                }
                else -> {
                    log.error("Exception occurred while approving user invitation: {} - ", id, e)

                    throw UserInvitationBoException(cause = e)
                }
            }
        }
    }

    /**
     * Reject the given invitation. If the user already exists, then delete the invitation but don't attempt to delete
     * user from Auth0 nor send a rejection email
     */
    fun reject(id: String) {
        try {
            val invitationTableRow = getInvitationTableRow(id)
            val user: AbstractUser = if(invitationTableRow.userType == UserRole.CLINICIAN) {
                gson.fromJson(invitationTableRow.userPayload, Clinician::class.java)
            }
            else {
                gson.fromJson(invitationTableRow.userPayload, Client::class.java)
            }

            userInvitationDao.delete(invitationTableRow)

            if(doesUserExistInDynamo(user)) {
                throw DuplicateResourceException("User with email ${user.email} already exists")
            }

            auth0ManagementService.deleteUser(invitationTableRow.auth0UserId)

            sendRejectionEmail(user)
        }
        catch (e: Exception) {
            when(e) {
                is ResourceNotFoundException, is DuplicateResourceException -> throw e
                else -> {
                    log.error("Exception while fetching/deleting from dynamodb or deleting from auth0: ", e)

                    throw UserInvitationBoException(cause = e)
                }
            }
        }
    }

    private fun doesUserExistInDynamo(user: AbstractUser): Boolean {
        val isClinician = user is Clinician

        // Check both clinician and clients if email is duplicate
        return clinicianBo.doesUserExist(user.email) || clientBo.doesUserExist(user.email)
    }

    private fun doesPhoneNumberExistInAuth0(phoneNumber: String): Boolean {
        return auth0ManagementService.getUserByPhoneNumber(phoneNumber) != null
    }

    private fun getInvitationTableRow(id: String): UserInvitationTableRow {
        return userInvitationDao.get(id)
            ?: throw ResourceNotFoundException(message = "Invitation not found: $id")
    }

    private fun createInvitationTableRow(id: String, auth0UserId: String, user: AbstractUser,
                                         referredByAuth0UserId: String): UserInvitationTableRow {
        val isClinician = user is Clinician

        return UserInvitationTableRow(
            id,
            userType = if (isClinician) UserRole.CLINICIAN else UserRole.CLIENT,
            auth0UserId,
            userPayload = gson.toJson(user),
            referredByAuth0UserId
        )
    }

    private fun sendInvitationEmail(user: AbstractUser, uid: String) {
        val isClinician = user is Clinician
        val approveUrl = "$invitationApproveUrl/$uid"
        val rejectUrl = "$invitationRejectionUrl/$uid"

        if (isClinician) {
            emailService.sendEmail(
                EmailUtils.ADMIN_EMAIL,
                EmailUtils.CLINICIAN_INVITATION_TO_ADMIN_SUBJECT,
                EmailUtils.getClinicianInvitationEmailForAdminBody(
                    user as Clinician,
                    approveUrl,
                    rejectUrl
                )
            )
        }
        else {
            emailService.sendEmail(
                user.email,
                EmailUtils.CLIENT_INVITATION_SUBJECT,
                EmailUtils.getClientInvitationEmailBody(
                    user as Client,
                    "$approveUrl?type=client",
                    rejectUrl
                )
            )
        }
    }

    private fun sendRejectionEmail(user: AbstractUser) {
        val isClinician = user is Clinician

        if (isClinician) {
            emailService.sendEmail(
                EmailUtils.ADMIN_EMAIL,
                EmailUtils.CLINICIAN_INVITATION_REJECTION_SUBJECT,
                EmailUtils.getClinicianInvitationRejectionEmail("${user.firstName} ${user.lastName}")
            )
        }
    }
}
