package ca.aversa.insessionservice.model.response

import ca.aversa.insessionservice.model.ClinicianType

data class GetClinicianSimpleProfileResponse(

    val firstName: String,
    val lastName: String,
    val type: ClinicianType
)
