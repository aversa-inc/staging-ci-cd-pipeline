package ca.aversa.insessionservice.model.request

import ca.aversa.insessionservice.model.SessionAttendeeStatus
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.Null
import javax.validation.constraints.Size

data class UpdateSessionAttendee(

    @JsonProperty("status")
    val status: SessionAttendeeStatus,

    @JsonProperty("note")
    @Null
    @field:Size(max = 250, message = "Note must be less than 250 characters")
    val note: String?
)
