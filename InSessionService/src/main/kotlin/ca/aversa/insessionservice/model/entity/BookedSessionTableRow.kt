package ca.aversa.insessionservice.model.entity

import ca.aversa.insessionservice.util.LocalDateTypeConverter
import ca.aversa.insessionservice.util.ZonedDateTimeTypeConverter
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import java.time.LocalDate
import java.time.ZonedDateTime

@DynamoDBDocument
class BookedSessionTableRow(

    @DynamoDBHashKey(attributeName = "id")
    var id: String = "",

    @DynamoDBIndexRangeKey(attributeName = "clinician", globalSecondaryIndexName = "session_date_index")
    var clinicianId: String = "",

    @DynamoDBIndexHashKey(attributeName = "session_date", globalSecondaryIndexName = "session_date_index")
    @DynamoDBTypeConverted(converter = LocalDateTypeConverter::class)
    var sessionDate: LocalDate = LocalDate.now(),

    @DynamoDBAttribute(attributeName = "start_date")
    @DynamoDBTypeConverted(converter = ZonedDateTimeTypeConverter::class)
    var startDateTime: ZonedDateTime = ZonedDateTime.now(),

    @DynamoDBAttribute(attributeName = "end_date")
    @DynamoDBTypeConverted(converter = ZonedDateTimeTypeConverter::class)
    var endDateTime: ZonedDateTime = ZonedDateTime.now(),

    @DynamoDBAttribute(attributeName = "subject")
    var subject: String = "",

    @DynamoDBAttribute(attributeName = "attendees")
    var attendees: MutableMap<String, SessionAttendeeDocument> = mutableMapOf(),

    @DynamoDBAttribute(attributeName = "requester")
    var requesterUserId: String = "",

    @DynamoDBAttribute(attributeName = "is_moderated")
    var isModerated: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BookedSessionTableRow

        if (id != other.id) return false
        if (clinicianId != other.clinicianId) return false
        if (sessionDate != other.sessionDate) return false
        if (startDateTime != other.startDateTime) return false
        if (endDateTime != other.endDateTime) return false
        if (subject != other.subject) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + clinicianId.hashCode()
        result = 31 * result + sessionDate.hashCode()
        result = 31 * result + startDateTime.hashCode()
        result = 31 * result + endDateTime.hashCode()
        result = 31 * result + subject.hashCode()
        return result
    }
}