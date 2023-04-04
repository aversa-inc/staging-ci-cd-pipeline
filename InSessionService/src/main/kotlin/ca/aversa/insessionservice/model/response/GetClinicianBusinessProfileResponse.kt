package ca.aversa.insessionservice.model.response

import ca.aversa.insessionservice.model.ClinicianBusinessProfile

data class GetClinicianBusinessProfileResponse(

    val businessProfile: ClinicianBusinessProfile?
)