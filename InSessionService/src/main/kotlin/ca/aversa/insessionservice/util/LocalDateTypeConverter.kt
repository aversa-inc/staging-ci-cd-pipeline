package ca.aversa.insessionservice.util

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import java.time.LocalDate

class LocalDateTypeConverter: DynamoDBTypeConverter<String, LocalDate> {

    override fun convert(localDate: LocalDate?): String {
        return localDate.toString()
    }

    override fun unconvert(localDate: String?): LocalDate {
        return LocalDate.parse(localDate)
    }
}