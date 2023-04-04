package ca.aversa.insessionservice.model.entity

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument

@DynamoDBDocument
data class ClinicianBusinessProfileDocument(

    @DynamoDBAttribute(attributeName = "street_number")
    var streetNumber: String = "",

    @DynamoDBAttribute(attributeName = "street_name")
    var streetName: String = "",

    @DynamoDBAttribute(attributeName = "city")
    var city: String = "",

    @DynamoDBAttribute(attributeName = "state")
    var state: String = "",

    @DynamoDBAttribute(attributeName = "country")
    var country: String = "",

    @DynamoDBAttribute(attributeName = "postal_code")
    var postalCode: String = "",

    @DynamoDBAttribute(attributeName = "tax_registration_number")
    var taxRegistrationNumber: String = ""
)
