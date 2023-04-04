package ca.aversa.insessionservice.model

import ca.aversa.insessionservice.model.entity.ClinicianBusinessProfileDocument
import ca.aversa.insessionservice.model.request.UpdateClinicianBusinessProfileRequest

data class ClinicianBusinessProfile(

    val streetNumber: String,
    val streetName: String,
    val city: String,
    val state: String,
    val country: String,
    val postalCode: String,
    val taxRegistrationNumber: String
) {
    fun toEntity(): ClinicianBusinessProfileDocument {
        return ClinicianBusinessProfileDocument(
            streetNumber, streetName, city, state, country, postalCode, taxRegistrationNumber
        )
    }

    object Mapper {
        fun from(request: UpdateClinicianBusinessProfileRequest): ClinicianBusinessProfile {
            return ClinicianBusinessProfile(
                request.streetNumber,
                request.streetName,
                request.city,
                request.state,
                request.country,
                request.postalCode,
                request.taxRegistrationNumber
            )
        }

        fun from(row: ClinicianBusinessProfileDocument): ClinicianBusinessProfile {
            return ClinicianBusinessProfile(
                row.streetNumber,
                row.streetName,
                row.city,
                row.state,
                row.country,
                row.postalCode,
                row.taxRegistrationNumber
            )
        }
    }
}