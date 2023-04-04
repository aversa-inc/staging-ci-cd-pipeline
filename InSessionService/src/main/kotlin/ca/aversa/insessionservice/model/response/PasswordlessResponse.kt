package ca.aversa.insessionservice.model.response

import com.fasterxml.jackson.annotation.JsonProperty

data class PasswordlessResponse(

    @JsonProperty("message")
    val message: String
)
