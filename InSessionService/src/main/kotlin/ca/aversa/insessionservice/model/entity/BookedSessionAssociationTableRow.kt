package ca.aversa.insessionservice.model.entity

import ca.aversa.insessionservice.util.ZonedDateTimeTypeConverter
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import java.time.ZonedDateTime

data class BookedSessionAssociationTableRow(

    @DynamoDBHashKey(attributeName = "id")
    var id: String = "",

    @DynamoDBRangeKey(attributeName = "sort_key")
    var sortKey: String = "",

    @DynamoDBAttribute(attributeName = "ttl")
    var ttl: Long = 0L,

    @DynamoDBAttribute(attributeName = "start_date")
    @DynamoDBTypeConverted(converter = ZonedDateTimeTypeConverter::class)
    var startDateTime: ZonedDateTime = ZonedDateTime.now(),

    @DynamoDBAttribute(attributeName = "end_date")
    @DynamoDBTypeConverted(converter = ZonedDateTimeTypeConverter::class)
    var endDateTime: ZonedDateTime = ZonedDateTime.now(),
)
