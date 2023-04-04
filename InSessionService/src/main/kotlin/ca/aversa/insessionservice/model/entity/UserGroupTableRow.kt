package ca.aversa.insessionservice.model.entity

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey

@DynamoDBDocument
data class UserGroupTableRow(

    @DynamoDBHashKey(attributeName = "id")
    var clinicianId: String = "",

    @DynamoDBRangeKey(attributeName = "sort_key")
    var groupId: String = "",

    @DynamoDBAttribute(attributeName = "clients")
    var clients: MutableList<String> = mutableListOf(),

    @DynamoDBAttribute(attributeName = "name")
    var name: String = ""
)
