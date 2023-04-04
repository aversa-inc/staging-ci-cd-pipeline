package ca.aversa.insessionservice.model.response

import com.fasterxml.jackson.annotation.JsonProperty

data class InviteClientResponse(

    @JsonProperty("message")
    val message: String = "Invitation request has been send to the client"
)
