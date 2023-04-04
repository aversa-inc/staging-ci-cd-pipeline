package ca.aversa.insessionservice.model.entity

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped

@DynamoDBDocument
data class UserCommonTableRow(

    @DynamoDBHashKey(attributeName = "id")
    var id: String = "",

    @DynamoDBAttribute(attributeName = "user_friendly_identifier")
    var userFriendlyIdentifier: String = "",

    @DynamoDBAttribute(attributeName = "gcm_config")
    var gcmConfigs: List<GcmConfigDocument> = listOf(),

    @DynamoDBAttribute(attributeName = "notifications_paused")
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL)
    var areNotificationsPaused: Boolean = false
)


