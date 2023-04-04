package ca.aversa.insessionservice.model.response

import ca.aversa.insessionservice.model.Group
import com.fasterxml.jackson.annotation.JsonProperty

data class GetClinicianGroupsResponse(

    @JsonProperty("groups")
    val groups: List<Group>
)
