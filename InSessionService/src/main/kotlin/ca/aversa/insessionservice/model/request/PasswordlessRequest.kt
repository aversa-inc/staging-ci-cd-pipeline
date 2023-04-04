package ca.aversa.insessionservice.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.Pattern

data class PasswordlessRequest(

    @JsonProperty("recipient", required = true)
    @field:Pattern(regexp = "^\\+1\\d{10}$", message = "Phone number must follow the format: +11111111111")
    val phoneNumber: String,

    @JsonProperty("body", required = true)
    val body: String,
)
