package ca.aversa.insessionservice.model.request

import ca.aversa.insessionservice.model.ClinicianType
import ca.aversa.insessionservice.model.UserGender
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.Email
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class RegisterClinicianRequest(

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

    @JsonProperty("collegeName", required = true)
    @field:Size(
        min = 5,
        max = 35,
        message = "College name must be between {min} and {max} characters"
    )
    val collegeName: String,

    @JsonProperty("registrationCode", required = true)
    @field:Pattern(regexp = "^[a-zA-Z0-9_]{5,10}$", message = "Registration code must be alphanumeric between 5-10 characters")
    val registrationCode: String,

    @JsonProperty("registrationYear", required = true)
    @field:Pattern(regexp = "^\\d{4}$", message = "Registration year must be 4 digits long")
    val registrationYear: String,

    @JsonProperty("type", required = true)
    val type: ClinicianType,

    @JsonProperty("gender")
    val gender: UserGender? = null,

    @JsonProperty("supervisorName")
    @field:Size(
        min = 2,
        max = 60,
        message = "Supervisor name must be between {min} and {max} characters"
    )
    val supervisorName: String? = null,
)
