package ca.aversa.insessionservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

data class UserSession (

    val clinicianId: String,
    val attendees: List<SessionAttendee>,
    val sessionId: String,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val subject: String,

    @get:JsonProperty("isModerated")
    @param:JsonProperty("isModerated")
    val isModerated: Boolean
)