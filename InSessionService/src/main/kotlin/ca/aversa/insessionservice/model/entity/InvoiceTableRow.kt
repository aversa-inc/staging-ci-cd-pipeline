package ca.aversa.insessionservice.model.entity

import ca.aversa.insessionservice.util.ZonedDateTimeInstantTypeConverter
import ca.aversa.insessionservice.util.ZonedDateTimeTypeConverter
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import java.time.ZoneId
import java.time.ZonedDateTime

@DynamoDBDocument
data class InvoiceTableRow(

    @DynamoDBHashKey(attributeName = "id")
    var userId: String = "",

    @DynamoDBRangeKey(attributeName = "sort_key")
    var invoiceId: String = "",

    @DynamoDBIndexHashKey(attributeName = "issued_by", globalSecondaryIndexName = "issued_by_index")
    var issuedByUserId: String = "",

    @DynamoDBIndexRangeKey(attributeName = "date", globalSecondaryIndexName = "issued_by_index")
    @DynamoDBTypeConverted(converter = ZonedDateTimeInstantTypeConverter::class)
    var date: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),

    @DynamoDBAttribute(attributeName = "session_start")
    @DynamoDBTypeConverted(converter = ZonedDateTimeTypeConverter::class)
    var sessionStartTime: ZonedDateTime = ZonedDateTime.now(),

    @DynamoDBAttribute(attributeName = "session_end")
    @DynamoDBTypeConverted(converter = ZonedDateTimeTypeConverter::class)
    var sessionEndTime: ZonedDateTime = ZonedDateTime.now(),

    @DynamoDBAttribute(attributeName = "business_profile")
    var businessProfileDocument: ClinicianBusinessProfileDocument? = null,

    @DynamoDBAttribute(attributeName = "title")
    var title: String = "",

    @DynamoDBAttribute(attributeName = "notes")
    var notes: String? = null,

    @DynamoDBAttribute(attributeName = "subtotal")
    var subtotal: Double = 0.0,

    @DynamoDBAttribute(attributeName = "tax")
    var tax: Double = 0.0,

    @DynamoDBAttribute(attributeName = "total")
    var total: Double = 0.0
)
