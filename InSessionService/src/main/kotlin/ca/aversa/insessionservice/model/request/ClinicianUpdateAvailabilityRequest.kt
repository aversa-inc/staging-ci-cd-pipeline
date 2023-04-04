package ca.aversa.insessionservice.model.request

import ca.aversa.insessionservice.model.Availability
import ca.aversa.insessionservice.model.Day
import com.fasterxml.jackson.annotation.JsonProperty

data class ClinicianUpdateAvailabilityRequest(

    @JsonProperty("availabilities")
    val availabilities: Map<Day, Availability>
)