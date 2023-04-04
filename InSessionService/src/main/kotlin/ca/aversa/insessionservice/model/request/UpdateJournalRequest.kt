package ca.aversa.insessionservice.model.request

import ca.aversa.insessionservice.util.ListPattern
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.validator.constraints.UniqueElements
import javax.validation.constraints.Size

data class UpdateJournalRequest(

    @JsonProperty("title", required = true)
    @field:Size(max = 100, message = "Title must be between 5 - 100 characters")
    val title: String,

    @JsonProperty("text", required = true)
    @field:Size(min = 0, max = 2000, message = "Text must be less than 2000 characters")
    val text: String,

    @JsonProperty("keywords")
    @field:ListPattern(regex = "^.{28}$", message = "Please provide valid keywords")
    @field:UniqueElements
    val keywords: List<String>
)
