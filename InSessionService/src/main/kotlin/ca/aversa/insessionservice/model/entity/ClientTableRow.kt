package ca.aversa.insessionservice.model.entity

import ca.aversa.insessionservice.model.UserGender
import ca.aversa.insessionservice.util.ZonedDateTimeInstantTypeConverter
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import java.time.ZonedDateTime

@DynamoDBDocument
class ClientTableRow(

    id: String = "",
    email: String = "",
    phoneNumber: String = "",
    firstName: String = "",
    lastName: String = "",

    @DynamoDBAttribute(attributeName = "clinician")
    var clinician: String = "",

    @DynamoDBAttribute(attributeName = "gender")
    @DynamoDBTypeConverted(converter = UserGenderTypeConverter::class)
    var gender: UserGender? = null,

    @DynamoDBAttribute(attributeName = "birth_date")
    @DynamoDBTypeConverted(converter = ZonedDateTimeInstantTypeConverter::class)
    var birthDate: ZonedDateTime? = null,

    @DynamoDBAttribute(attributeName = "family_members")
    var familyMembers: List<UserFamilyMemberDocument> = mutableListOf()
): AbstractUserTableRow(id, email, phoneNumber, firstName, lastName)