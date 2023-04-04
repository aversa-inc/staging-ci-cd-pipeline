package ca.aversa.insessionservice.model.response

import com.fasterxml.jackson.annotation.JsonProperty

data class InviteClinicianResponse(

    @JsonProperty("message")
    val message: String = "Invitation request has been received. We will send you an email with an update in the next few business days"
)