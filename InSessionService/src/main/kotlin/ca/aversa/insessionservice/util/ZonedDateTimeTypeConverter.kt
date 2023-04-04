package ca.aversa.insessionservice.util

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ZonedDateTimeTypeConverter: DynamoDBTypeConverter<String, ZonedDateTime> {

    override fun convert(zonedDateTime: ZonedDateTime?): String {
        return zonedDateTime!!.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    override fun unconvert(dateTime: String?): ZonedDateTime {
        return ZonedDateTime.parse(dateTime).withZoneSameInstant(ZoneId.of("UTC"))
    }
}