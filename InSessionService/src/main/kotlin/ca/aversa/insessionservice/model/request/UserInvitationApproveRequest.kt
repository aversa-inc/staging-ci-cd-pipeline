package ca.aversa.insessionservice.model.request

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import javax.validation.constraints.Size

data class UserInvitationApproveRequest(

    @PathVariable
    @field:Size(
        min = 36,
        max = 36,
        message = "Invalid identifier provided"
    )
    val id: String,

    @RequestParam
    val type: String? = null
)
