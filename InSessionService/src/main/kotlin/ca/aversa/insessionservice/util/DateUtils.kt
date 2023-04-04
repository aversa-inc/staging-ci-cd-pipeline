package ca.aversa.insessionservice.util

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DateUtils {

    private val userReadableFormatter = DateTimeFormatter.ofPattern("E dd MMM yyyy hh:mm a");

    fun createUserReadableDate(date: ZonedDateTime, timeZone: ZoneId = ZoneId.of("America/New_York")): String {
        return date.withZoneSameInstant(timeZone).format(userReadableFormatter)
    }
}
