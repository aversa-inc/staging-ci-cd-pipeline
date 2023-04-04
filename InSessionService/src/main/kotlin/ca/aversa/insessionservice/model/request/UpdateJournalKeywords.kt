package ca.aversa.insessionservice.model.request

import ca.aversa.insessionservice.util.ListPattern
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.validator.constraints.UniqueElements

data class UpdateJournalKeywords(

    @JsonProperty("keywords")
    @field:ListPattern(regex = "^.{28}$", message = "Please provide valid keywords")
    @field:UniqueElements
    val keywords: List<String>
)
