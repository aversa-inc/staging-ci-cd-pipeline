package ca.aversa.insessionservice.model.response

import ca.aversa.insessionservice.model.ClinicianType
import ca.aversa.insessionservice.model.UserGender

data class GetClinicianFullProfileResponse(

    val email: String,
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val collegeName: String,
    val registrationCode: String,
    val registrationYear: String,
    val type: ClinicianType,
    val gender: UserGender?,
    val supervisorName: String?
)
