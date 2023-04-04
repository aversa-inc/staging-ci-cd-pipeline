package ca.aversa.insessionservice.config

import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.dao.impl.BasicDaoImpl
import ca.aversa.insessionservice.model.entity.BookedSessionAssociationTableRow
import ca.aversa.insessionservice.model.entity.BookedSessionTableRow
import ca.aversa.insessionservice.model.entity.ClientTableRow
import ca.aversa.insessionservice.model.entity.ClinicianTableRow
import ca.aversa.insessionservice.model.entity.InvoiceTableRow
import ca.aversa.insessionservice.model.entity.UserCommonTableRow
import ca.aversa.insessionservice.model.entity.UserGroupTableRow
import ca.aversa.insessionservice.model.entity.UserInvitationTableRow
import ca.aversa.insessionservice.model.entity.UserJournalTableRow
import ca.aversa.insessionservice.util.Constants
import ca.aversa.insessionservice.util.EnvironmentUtils
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DaoConfig {

    @Bean
    fun getAmazonDynamoDb(region: Regions): AmazonDynamoDB {
        return AmazonDynamoDBClientBuilder.standard()
            .withRegion(region)
            .build()
    }

    @Bean
    @Qualifier(Constants.CLINICIAN_DAO_BEAN_QUALIFIER)
    fun getClinicianDao(amazonDynamoDB: AmazonDynamoDB): BasicDao<ClinicianTableRow> {
        val tableName = EnvironmentUtils.extractEnvironmentVariable("CLINICIAN_TABLE_NAME")
        val config = DynamoDBMapperConfig.Builder()
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride(tableName))
            .withPaginationLoadingStrategy(DynamoDBMapperConfig.PaginationLoadingStrategy.EAGER_LOADING)
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .build()
        val mapper = DynamoDBMapper(amazonDynamoDB, config)

        return BasicDaoImpl(mapper, ClinicianTableRow::class.java, tableName)
    }

    @Bean
    @Qualifier(Constants.CLIENT_DAO_BEAN_QUALIFIER)
    fun getClientDao(amazonDynamoDB: AmazonDynamoDB): BasicDao<ClientTableRow> {
        val tableName = EnvironmentUtils.extractEnvironmentVariable("CLIENT_TABLE_NAME")
        val config = DynamoDBMapperConfig.Builder()
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride(tableName))
            .withPaginationLoadingStrategy(DynamoDBMapperConfig.PaginationLoadingStrategy.EAGER_LOADING)
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .build()
        val mapper = DynamoDBMapper(amazonDynamoDB, config)

        return BasicDaoImpl(mapper, ClientTableRow::class.java, tableName)
    }

    @Bean
    @Qualifier(Constants.USER_INVITATION_DAO_BEAN_QUALIFIER)
    fun getUserInvitationDao(amazonDynamoDB: AmazonDynamoDB): BasicDao<UserInvitationTableRow> {
        val tableName = EnvironmentUtils.extractEnvironmentVariable("USER_INVITATION_TABLE_NAME")
        val config = DynamoDBMapperConfig.Builder()
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride(tableName))
            .build()
        val mapper = DynamoDBMapper(amazonDynamoDB, config)

        return BasicDaoImpl(mapper, UserInvitationTableRow::class.java, tableName)
    }

    @Bean
    @Qualifier(Constants.BOOKED_SESSIONS_DAO_BEAN_QUALIFIER)
    fun getBookedSessionsDao(amazonDynamoDB: AmazonDynamoDB): BasicDao<BookedSessionTableRow> {
        val tableName = EnvironmentUtils.extractEnvironmentVariable("BOOKED_SESSIONS_TABLE_NAME")
        val config = DynamoDBMapperConfig.Builder()
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride(tableName))
            .build()
        val mapper = DynamoDBMapper(amazonDynamoDB, config)

        return BasicDaoImpl(mapper, BookedSessionTableRow::class.java, tableName)
    }

    @Bean
    @Qualifier(Constants.BOOKED_SESSIONS_ASSOCIATION_DAO_BEAN_QUALIFIER)
    fun getBookedSessionsAssociationsDao(amazonDynamoDB: AmazonDynamoDB): BasicDao<BookedSessionAssociationTableRow> {
        val tableName = EnvironmentUtils.extractEnvironmentVariable("BOOKED_SESSIONS_ASSOCIATION_TABLE_NAME")
        val config = DynamoDBMapperConfig.Builder()
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride(tableName))
            .build()
        val mapper = DynamoDBMapper(amazonDynamoDB, config)

        return BasicDaoImpl(mapper, BookedSessionAssociationTableRow::class.java, tableName)
    }

    @Bean
    @Qualifier(Constants.USER_COMMON_DAO_BEAN_QUALIFIER)
    fun getUserCommonDao(amazonDynamoDB: AmazonDynamoDB): BasicDao<UserCommonTableRow> {
        val tableName = EnvironmentUtils.extractEnvironmentVariable("USER_COMMON_TABLE_NAME")
        val config = DynamoDBMapperConfig.Builder()
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride(tableName))
            .build()
        val mapper = DynamoDBMapper(amazonDynamoDB, config)

        return BasicDaoImpl(mapper, UserCommonTableRow::class.java, tableName)
    }

    @Bean
    @Qualifier(Constants.USER_GROUP_DAO_BEAN_QUALIFIER)
    fun getUserGroupDao(amazonDynamoDB: AmazonDynamoDB): BasicDao<UserGroupTableRow> {
        val tableName = EnvironmentUtils.extractEnvironmentVariable("USER_GROUP_TABLE_NAME")
        val config = DynamoDBMapperConfig.Builder()
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride(tableName))
            .build()
        val mapper = DynamoDBMapper(amazonDynamoDB, config)

        return BasicDaoImpl(mapper, UserGroupTableRow::class.java, tableName)
    }

    @Bean
    @Qualifier(Constants.USER_JOURNAL_DAO_BEAN_QUALIFIER)
    fun getUserJournalDao(amazonDynamoDB: AmazonDynamoDB): BasicDao<UserJournalTableRow> {
        val tableName = EnvironmentUtils.extractEnvironmentVariable("USER_JOURNAL_TABLE_NAME")
        val config = DynamoDBMapperConfig.Builder()
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride(tableName))
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .build()
        val mapper = DynamoDBMapper(amazonDynamoDB, config)

        return BasicDaoImpl(mapper, UserJournalTableRow::class.java, tableName)
    }

    @Bean
    @Qualifier(Constants.INVOICE_DAO_BEAN_QUALIFIER)
    fun getInvoiceDao(amazonDynamoDB: AmazonDynamoDB): BasicDao<InvoiceTableRow> {
        val tableName = EnvironmentUtils.extractEnvironmentVariable("INVOICE_TABLE_NAME")
        val config = DynamoDBMapperConfig.Builder()
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride(tableName))
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .build()
        val mapper = DynamoDBMapper(amazonDynamoDB, config)

        return BasicDaoImpl(mapper, InvoiceTableRow::class.java, tableName)
    }
}