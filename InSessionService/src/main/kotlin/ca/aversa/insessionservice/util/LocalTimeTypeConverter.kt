package ca.aversa.insessionservice.util

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import java.time.LocalTime

class LocalTimeTypeConverter: DynamoDBTypeConverter<String, LocalTime> {

    override fun convert(localTime: LocalTime?): String {
        return localTime.toString()
    }

    override fun unconvert(localtime: String?): LocalTime {
        return LocalTime.parse(localtime)
    }
}