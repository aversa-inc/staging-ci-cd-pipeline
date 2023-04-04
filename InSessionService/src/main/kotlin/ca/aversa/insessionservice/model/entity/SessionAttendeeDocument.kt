package ca.aversa.insessionservice.model.entity

import ca.aversa.insessionservice.model.SessionAttendeeStatus
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter

@DynamoDBDocument
data class SessionAttendeeDocument(

    @DynamoDBAttribute(attributeName = "user_id")
    var userId: String = "",

    @DynamoDBAttribute(attributeName = "status")
    @DynamoDBTypeConverted(converter = SessionAttendeeStatusTypeConverter::class)
    var status: SessionAttendeeStatus = SessionAttendeeStatus.TENTATIVE,

    @DynamoDBAttribute(attributeName = "note")
    var note: String? = null
)

class SessionAttendeeStatusTypeConverter: DynamoDBTypeConverter<String, SessionAttendeeStatus> {

    override fun convert(status: SessionAttendeeStatus?): String {
        return status!!.name
    }

    override fun unconvert(status: String?): SessionAttendeeStatus {
        if(status == null) {
            throw RuntimeException("Session attendee status is null in DDB")
        }

        return SessionAttendeeStatus.valueOf(status)
    }
}