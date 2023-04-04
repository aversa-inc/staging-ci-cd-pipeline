package ca.aversa.insessionservice.model.entity

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument

@DynamoDBDocument
data class GcmConfigDocument(

    @DynamoDBAttribute(attributeName = "gcm_device_sns_endpoint")
    var gcmDeviceSnsEndpoint: String = "",
)
