package ca.aversa.insessionservice.model.request

import ca.aversa.insessionservice.util.FutureZonedDateTime
import ca.aversa.insessionservice.util.ListPattern
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.validator.constraints.UniqueElements
import org.springframework.format.annotation.DateTimeFormat
import java.time.ZonedDateTime
import javax.validation.constraints.Pattern

data class CreateSessionRequest(

    @JsonProperty("clinician")
    @field:Pattern(regexp = "^.{28}$", message = "Please provide a valid clinician Id")
    val clinicianId: String,

    @JsonProperty("clients")
    @field:ListPattern(regex = "^.{28}$", message = "Please provide valid client Ids")
    @field:UniqueElements
    val clientIds: List<String>,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty("startDateTime")
    @field:FutureZonedDateTime
    val startDateTime: ZonedDateTime,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty("endDateTime")
    @field:FutureZonedDateTime
    val endDateTime: ZonedDateTime,

    @JsonProperty("subject", required = false)
    @field:Pattern(regexp = "^.{0,100}$", message = "Subject must be less than 100 characters")
    val subject: String,

    @get:JsonProperty("isModerated", required = false)
    @param:JsonProperty("isModerated", required = false)
    val isModerated: Boolean = false,
)
