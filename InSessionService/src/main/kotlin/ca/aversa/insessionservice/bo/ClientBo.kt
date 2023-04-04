package ca.aversa.insessionservice.bo

import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.exception.DuplicateResourceException
import ca.aversa.insessionservice.exception.RegisterUserException
import ca.aversa.insessionservice.model.Client
import ca.aversa.insessionservice.model.entity.ClientTableRow
import ca.aversa.insessionservice.service.EmailService
import ca.aversa.insessionservice.util.EmailUtils

open class ClientBo(

    private val userBo: UserBo<Client, ClientTableRow>,
    private val emailService: EmailService,
    private val clientDao: BasicDao<ClientTableRow>
) {

    fun doesUserExist(email: String): Boolean {
        return userBo.doesUserExist(email)
    }

    fun register(user: Client, auth0UserId: String, clinicianId: String) {
        try {
            user.clinicianId = clinicianId
            userBo.register(user, auth0UserId)

            emailService.sendEmail(
                user.email,
                EmailUtils.CLIENT_INVITATION_ACCEPT_SUBJECT,
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
     * Update client attributes in DynamoDB
     */
    fun updateProfile(user: Client, auth0UserId: String): Client {
        return userBo.updateProfile(user, auth0UserId)
    }

    fun updateFcmToken(clinicianUserId: String, fcmToken: String) {
        userBo.updateFcmToken(clinicianUserId, fcmToken)
    }

    fun getProfiles(userIds: List<String>): List<Client> {
        val clientTableRows = clientDao.batchLoad(userIds)

        return clientTableRows.map(Client.Mapper::from)
    }
}