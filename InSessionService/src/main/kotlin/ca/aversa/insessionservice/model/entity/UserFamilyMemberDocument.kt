package ca.aversa.insessionservice.model.entity

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument

@DynamoDBDocument
data class UserFamilyMemberDocument(

    @DynamoDBAttribute(attributeName = "email")
    var email: String = "",

    @DynamoDBAttribute(attributeName = "phone_number")
    var phoneNumber: String = "",

    @DynamoDBAttribute(attributeName = "first_name")
    var firstName: String = "",

    @DynamoDBAttribute(attributeName = "last_name")
    var lastName: String = "",

    @DynamoDBAttribute(attributeName = "emergency_contact")
    var emergencyContact: Boolean = false,
)
