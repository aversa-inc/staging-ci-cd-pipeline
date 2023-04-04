package ca.aversa.insessionservice.model.response

import ca.aversa.insessionservice.model.AbstractUser
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserProfileUpdateResponse(

    val user: AbstractUser? = null,
    val fieldErrors: Map<String, String>? = null
)
