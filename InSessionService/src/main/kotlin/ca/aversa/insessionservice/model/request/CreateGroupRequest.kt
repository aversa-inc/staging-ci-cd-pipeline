package ca.aversa.insessionservice.model.request

import ca.aversa.insessionservice.util.ListPattern
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.validator.constraints.UniqueElements
import javax.validation.constraints.Pattern

data class CreateGroupRequest(

    @JsonProperty("clients")
    @field:ListPattern(regex = "^.{28}$", message = "Please provide valid client Ids")
    @field:UniqueElements
    val clientIds: List<String>,

    @JsonProperty("name", required = false)
    @field:Pattern(regexp = "^.{0,50}$", message = "Name must be less than 50 characters")
    val name: String,
)
