package ca.aversa.insessionservice.model.response

import ca.aversa.insessionservice.model.Availability
import ca.aversa.insessionservice.model.Day
import com.fasterxml.jackson.annotation.JsonProperty

data class GetClinicianAvailabilityResponse(

    @JsonProperty("availabilities")
    val availabilities: Map<Day, Availability>
)
