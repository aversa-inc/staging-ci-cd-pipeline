package ca.aversa.insessionservice.config

import ca.aversa.insessionservice.bo.AuthenticationBo
import ca.aversa.insessionservice.bo.ClientBo
import ca.aversa.insessionservice.bo.ClinicianBo
import ca.aversa.insessionservice.bo.InvoiceBo
import ca.aversa.insessionservice.bo.JournalBo
import ca.aversa.insessionservice.bo.SessionBo
import ca.aversa.insessionservice.bo.UserBo
import ca.aversa.insessionservice.bo.UserInvitationBo
import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.model.entity.BookedSessionAssociationTableRow
import ca.aversa.insessionservice.model.entity.BookedSessionTableRow
import ca.aversa.insessionservice.model.entity.ClientTableRow
import ca.aversa.insessionservice.model.entity.ClinicianTableRow
import ca.aversa.insessionservice.model.entity.InvoiceTableRow
import ca.aversa.insessionservice.model.entity.UserCommonTableRow
import ca.aversa.insessionservice.model.entity.UserGroupTableRow
import ca.aversa.insessionservice.model.entity.UserInvitationTableRow
import ca.aversa.insessionservice.model.entity.UserJournalTableRow
import ca.aversa.insessionservice.service.Auth0ManagementService
import ca.aversa.insessionservice.service.EmailService
import ca.aversa.insessionservice.service.PushNotificationService
import ca.aversa.insessionservice.util.Constants
import ca.aversa.insessionservice.util.EnvironmentUtils
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class BoConfig {

    @Bean
    fun getClientBo(
        @Qualifier(Constants.CLIENT_DAO_BEAN_QUALIFIER) clientDao: BasicDao<ClientTableRow>,
        @Qualifier(Constants.USER_COMMON_DAO_BEAN_QUALIFIER) userCommonDao: BasicDao<UserCommonTableRow>,
        auth0ManagementService: Auth0ManagementService,
        emailService: EmailService,
        pushNotificationService: PushNotificationService
    ): ClientBo {
        val emailGsiIndexName = EnvironmentUtils.extractEnvironmentVariable("USER_TABLE_EMAIL_GSI_NAME")
        val phoneGsiIndexName = EnvironmentUtils.extractEnvironmentVariable("USER_TABLE_PHONE_NUMBER_GSI_NAME")

        return ClientBo(
            UserBo(
                emailGsiIndexName,
                phoneGsiIndexName,
                clientDao,
                userCommonDao,
                auth0ManagementService,
                pushNotificationService
            ),
            emailService,
            clientDao
        )
    }

    @Bean
    fun getClinicianBo(
        @Qualifier(Constants.CLINICIAN_DAO_BEAN_QUALIFIER) clinicianDao: BasicDao<ClinicianTableRow>,
        @Qualifier(Constants.USER_COMMON_DAO_BEAN_QUALIFIER) userCommonDao: BasicDao<UserCommonTableRow>,
        @Qualifier(Constants.USER_GROUP_DAO_BEAN_QUALIFIER) groupDao: BasicDao<UserGroupTableRow>,
        auth0ManagementService: Auth0ManagementService,
        emailService: EmailService,
        pushNotificationService: PushNotificationService
    ): ClinicianBo {
        val emailGsiIndexName = EnvironmentUtils.extractEnvironmentVariable("USER_TABLE_EMAIL_GSI_NAME")
        val phoneGsiIndexName = EnvironmentUtils.extractEnvironmentVariable("USER_TABLE_PHONE_NUMBER_GSI_NAME")

        return ClinicianBo(
            UserBo(
                emailGsiIndexName,
                phoneGsiIndexName,
                clinicianDao,
                userCommonDao,
                auth0ManagementService,
                pushNotificationService
            ),
            clinicianDao,
            groupDao,
            emailService
        )
    }

    @Bean
    fun getAuthenticationBo(
        clientBo: ClientBo,
        clinicianBo: ClinicianBo
    ): AuthenticationBo {
        return AuthenticationBo()
    }

    @Bean
    fun getUserInvitationBo(
        @Qualifier(Constants.USER_INVITATION_DAO_BEAN_QUALIFIER) invitationDao: BasicDao<UserInvitationTableRow>,
        clinicianBo: ClinicianBo,
        clientBo: ClientBo,
        auth0ManagementService: Auth0ManagementService,
        emailService: EmailService,
        gson: Gson,
        appConfig: AppConfig
    ): UserInvitationBo {
        return UserInvitationBo(
            clinicianBo, clientBo, invitationDao, auth0ManagementService, emailService, gson, appConfig.siteUrl
        )
    }

    @Bean
    fun getSessionBo(
        @Qualifier(Constants.BOOKED_SESSIONS_DAO_BEAN_QUALIFIER) sessionDao: BasicDao<BookedSessionTableRow>,
        @Qualifier(Constants.BOOKED_SESSIONS_ASSOCIATION_DAO_BEAN_QUALIFIER) sessionAssociationDao: BasicDao<BookedSessionAssociationTableRow>,
        @Qualifier(Constants.USER_COMMON_DAO_BEAN_QUALIFIER) userCommonDao: BasicDao<UserCommonTableRow>,
        pushNotificationService: PushNotificationService
    ): SessionBo {
        val sessionDateIndexName = EnvironmentUtils.extractEnvironmentVariable("BOOKED_SESSIONS_DATE_GSI_NAME")

        return SessionBo(
            sessionDateIndexName,
            sessionDao,
            sessionAssociationDao,
            userCommonDao,
            pushNotificationService
        )
    }

    @Bean
    fun getJournalBo(
        @Qualifier(Constants.USER_JOURNAL_DAO_BEAN_QUALIFIER) journalDao: BasicDao<UserJournalTableRow>
    ): JournalBo {
        return JournalBo(journalDao)
    }

    @Bean
    fun getInvoiceBo(
        clinicianBo: ClinicianBo,
        clientBo: ClientBo,
        @Qualifier(Constants.INVOICE_DAO_BEAN_QUALIFIER) invoiceDao: BasicDao<InvoiceTableRow>
    ): InvoiceBo {
        return InvoiceBo(clinicianBo, clientBo, invoiceDao)
    }
}