package ca.aversa.insessionservice.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.Email
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class ClinicianUpdateClientProfileRequest(

    @JsonProperty("email", required = true)
    @field:Email(regexp = "^.+[@].+[\\\\.].+$", message = "Email must follow the format: abc@abc.com")
    @field:Size(
        min = 5,
        max = 254,
        message = "Email must be between {min} and {max} characters"
    )
    val email: String,

    @JsonProperty("phoneNumber", required = true)
    @field:Pattern(regexp = "^\\+1\\d{10}$", message = "Phone number must follow the format: +11111111111")
    val phoneNumber: String,

    @JsonProperty("firstName", required = true)
    @field:Size(
        min = 2,
        max = 35,
        message = "First name must be between {min} and {max} characters"
    )
    val firstName: String,

    @JsonProperty("lastName", required = true)
    @field:Size(
        min = 2,
        max = 35,
        message = "Last name must be between {min} and {max} characters"
    )
    val lastName: String,
)
