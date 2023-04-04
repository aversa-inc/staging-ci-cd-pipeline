package ca.aversa.insessionservice.util

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class ZonedDateTimeInstantTypeConverter: DynamoDBTypeConverter<Long, ZonedDateTime> {

    override fun convert(zonedDateTime: ZonedDateTime?): Long {
        return zonedDateTime!!.toInstant().epochSecond;
    }

    override fun unconvert(instant: Long?): ZonedDateTime {
        return Instant.ofEpochSecond(instant!!).atZone(ZoneId.of("UTC"));
    }
}