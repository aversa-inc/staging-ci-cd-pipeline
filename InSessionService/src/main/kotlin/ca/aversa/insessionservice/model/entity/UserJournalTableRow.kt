package ca.aversa.insessionservice.model.entity

import ca.aversa.insessionservice.util.ZonedDateTimeTypeConverter
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import java.time.ZonedDateTime

data class UserJournalTableRow(

    @DynamoDBHashKey(attributeName = "id")
    var userId: String = "",

    @DynamoDBRangeKey(attributeName = "sort_key")
    var id: String = "",

    @DynamoDBAttribute(attributeName = "title")
    var title: String = "",

    @DynamoDBAttribute(attributeName = "text")
    var text: String = "",

    @DynamoDBAttribute(attributeName = "date")
    @DynamoDBTypeConverted(converter = ZonedDateTimeTypeConverter::class)
    var updatedDateTime: ZonedDateTime = ZonedDateTime.now(),

    @DynamoDBAttribute(attributeName = "keywords")
    var keywords: List<String> = mutableListOf()
)