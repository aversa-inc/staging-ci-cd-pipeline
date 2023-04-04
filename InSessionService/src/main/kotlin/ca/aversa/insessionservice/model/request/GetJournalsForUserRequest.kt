package ca.aversa.insessionservice.model.request

import org.springframework.web.bind.annotation.RequestParam
import javax.validation.constraints.Null

data class GetJournalsForUserRequest(

    @Null
    @RequestParam("keywords")
    val keywords: String?
)
