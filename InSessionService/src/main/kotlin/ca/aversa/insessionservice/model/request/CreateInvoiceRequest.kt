package ca.aversa.insessionservice.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.format.annotation.DateTimeFormat
import java.time.ZonedDateTime
import javax.validation.constraints.Min
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class CreateInvoiceRequest(

    @JsonProperty("title", required = true)
    @field:Size(max = 100, message = "Title must be between 5 - 100 characters")
    val title: String,

    @JsonProperty("userId", required = true)
    @field:Pattern(regexp = "^.{28}$", message = "Please provide a valid user Id")
    val userId: String,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty("sessionStart")
    val sessionStart: ZonedDateTime,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty("sessionEnd")
    val sessionEnd: ZonedDateTime,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty("date")
    val date: ZonedDateTime,

    @JsonProperty("notes")
    @field:Size(max = 250, message = "Notes must be less than 250 characters")
    val notes: String?,

    @JsonProperty("subtotal", required = true)
    @field:Min(0, message = "Subtotal must be greater than 0")
    val subtotal: Double,
)
