package ca.aversa.insessionservice.model.entity

import ca.aversa.insessionservice.util.LocalTimeTypeConverter
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import java.time.LocalTime

@DynamoDBDocument
data class AvailabilityDocument(

    @DynamoDBAttribute(attributeName = "start_time")
    @DynamoDBTypeConverted(converter = LocalTimeTypeConverter::class)
    var startTime: LocalTime? = null,

    @DynamoDBAttribute(attributeName = "end_time")
    @DynamoDBTypeConverted(converter = LocalTimeTypeConverter::class)
    var endTime: LocalTime? = null
)
