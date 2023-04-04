package ca.aversa.insessionservice.model.entity

import ca.aversa.insessionservice.model.UserGender
import ca.aversa.insessionservice.util.ZonedDateTimeTypeConverter
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import java.time.ZoneId
import java.time.ZonedDateTime

@DynamoDBDocument
abstract class AbstractUserTableRow(

    @DynamoDBHashKey(attributeName = "id")
    var id: String = "",

    @DynamoDBIndexHashKey(attributeName = "email", globalSecondaryIndexName = "email_index")
    var email: String = "",

    @DynamoDBIndexHashKey(attributeName = "phone_number", globalSecondaryIndexName = "phone_number_index")
    var phoneNumber: String = "",

    @DynamoDBAttribute(attributeName = "first_name")
    var firstName: String = "",

    @DynamoDBAttribute(attributeName = "last_name")
    var lastName: String = "",

    @DynamoDBAttribute(attributeName = "last_updated")
    @DynamoDBTypeConverted(converter = ZonedDateTimeTypeConverter::class)
    var lastUpdated: ZonedDateTime? = ZonedDateTime.now(ZoneId.of("UTC"))
)

class UserGenderTypeConverter : DynamoDBTypeConverter<String, UserGender> {

    override fun convert(userGender: UserGender?): String {
        return userGender!!.name
    }

    override fun unconvert(userGender: String?): UserGender {
        if (userGender == null) {
            throw RuntimeException("Invalid user gender found in DDB $userGender")
        }

        return UserGender.valueOf(userGender)
    }
}