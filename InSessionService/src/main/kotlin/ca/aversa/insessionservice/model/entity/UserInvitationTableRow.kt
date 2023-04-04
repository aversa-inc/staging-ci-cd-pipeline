package ca.aversa.insessionservice.model.entity

import ca.aversa.insessionservice.model.UserRole
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter

class UserInvitationTableRow(

    @DynamoDBHashKey(attributeName = "id")
    var id: String = "",

    @DynamoDBAttribute(attributeName = "user_type")
    @DynamoDBTypeConverted(converter = UserRoleConverter::class)
    var userType: UserRole = UserRole.CLINICIAN,

    @DynamoDBAttribute(attributeName = "auth0_user_id")
    var auth0UserId: String = "",

    @DynamoDBAttribute(attributeName = "user_payload")
    var userPayload: String = "",

    @DynamoDBAttribute(attributeName = "referred_by")
    var referredBy: String = ""
)

class UserRoleConverter: DynamoDBTypeConverter<String, UserRole> {

    override fun convert(userRole: UserRole?): String {
        return userRole!!.name
    }

    override fun unconvert(userRole: String?): UserRole {
        if(userRole == null) {
            throw RuntimeException("Invalid user role found in DDB {}", userRole)
        }

        return UserRole.valueOf(userRole)
    }
}
