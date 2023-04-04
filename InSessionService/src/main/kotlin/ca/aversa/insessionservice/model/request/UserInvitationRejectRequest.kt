package ca.aversa.insessionservice.model.request

import org.springframework.web.bind.annotation.PathVariable
import javax.validation.constraints.Size

data class UserInvitationRejectRequest(

    @PathVariable
    @field:Size(
        min = 36,
        max = 36,
        message = "Invalid identifier provided"
    )
    val id: String
)
