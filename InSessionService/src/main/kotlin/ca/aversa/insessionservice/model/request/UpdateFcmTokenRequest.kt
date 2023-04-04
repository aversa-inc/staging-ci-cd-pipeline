package ca.aversa.insessionservice.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.Size

data class UpdateFcmTokenRequest(

    @JsonProperty("fcmToken", required = true)
    @field:Size(min = 5, max = 4096)
    val fcmToken: String
)
