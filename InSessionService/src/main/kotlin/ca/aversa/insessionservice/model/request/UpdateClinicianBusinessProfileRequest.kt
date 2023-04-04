package ca.aversa.insessionservice.model.request

import com.fasterxml.jackson.annotation.JsonProperty

data class UpdateClinicianBusinessProfileRequest(

    @JsonProperty("streetNumber", required = true)
    val streetNumber: String,

    @JsonProperty("streetName", required = true)
    val streetName: String,

    @JsonProperty("city", required = true)
    val city: String,

    @JsonProperty("state", required = true)
    val state: String,

    @JsonProperty("country", required = true)
    val country: String,

    @JsonProperty("postalCode", required = true)
    val postalCode: String,

    @JsonProperty("taxRegistrationNumber", required = true)
    val taxRegistrationNumber: String
)
