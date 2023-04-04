package ca.aversa.insessionservice.model

import ca.aversa.insessionservice.model.entity.AbstractUserTableRow
import ca.aversa.insessionservice.model.entity.ClientTableRow
import ca.aversa.insessionservice.model.entity.ClinicianTableRow
import ca.aversa.insessionservice.model.request.ClientProfileUpdateRequest
import ca.aversa.insessionservice.model.request.ClinicianProfileUpdateRequest
import ca.aversa.insessionservice.model.request.RegisterClientRequest
import ca.aversa.insessionservice.model.request.RegisterClinicianRequest
import com.auth0.json.mgmt.users.User
import java.time.ZonedDateTime


abstract class AbstractUser(
    val email: String,
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val auth0UserId: String?
) {
    abstract fun toEntity(auth0UserId: String): AbstractUserTableRow

    open fun toAuth0User(): User {
        val user = User("sms")
        user.name = "$firstName $lastName"
        user.nickname = firstName
        user.email = email
        user.phoneNumber = phoneNumber

        return user
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractUser

        if (email != other.email) return false
        if (phoneNumber != other.phoneNumber) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = email.hashCode()
        result = 31 * result + phoneNumber.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        return result
    }
}

class Clinician(

    email: String,
    phoneNumber: String,
    firstName: String,
    lastName: String,

    val collegeName: String,
    val registrationCode: String,
    val registrationYear: String,
    val type: ClinicianType,
    val businessProfile: ClinicianBusinessProfile?,
    val availability: ClinicianAvailability?,
    val gender: UserGender?,
    val supervisorName: String?,

    auth0UserId: String? = null,
) : AbstractUser(email, phoneNumber, firstName, lastName, auth0UserId) {

    override fun toEntity(auth0UserId: String): ClinicianTableRow {
        val clinicianAvailability = availability ?: ClinicianAvailability()

        return ClinicianTableRow(
            auth0UserId, email, phoneNumber, firstName, lastName,
            collegeName, registrationCode, registrationYear, type,
            businessProfile?.toEntity(), clinicianAvailability.toEntityAvailabilities(),
            gender = gender, supervisorName =  supervisorName
        )
    }

    override fun toAuth0User(): User {
        val user = super.toAuth0User()
        user.userMetadata = mapOf(
            "role" to UserRole.CLINICIAN
        )

        return user
    }

    object Mapper {

        fun from(request: RegisterClinicianRequest): Clinician {
            return Clinician(
                request.email.lowercase(),
                request.phoneNumber,
                request.firstName,
                request.lastName,
                request.collegeName,
                request.registrationCode,
                request.registrationYear,
                request.type,
                null,
                ClinicianAvailability(),
                request.gender,
                request.supervisorName,
            )
        }

        fun from(request: ClinicianProfileUpdateRequest): Clinician {
            return Clinician(
                request.email.lowercase(),
                request.phoneNumber,
                request.firstName,
                request.lastName,
                request.collegeName,
                request.registrationCode,
                request.registrationYear,
                request.type,
                null,
                null,
                request.gender,
                request.supervisorName
            )
        }

        fun from(clinicianTableRow: ClinicianTableRow): Clinician {
            val businessProfile = clinicianTableRow.businessProfile?.let(ClinicianBusinessProfile.Mapper::from)
            val availability = clinicianTableRow.availabilities.let(ClinicianAvailability.Mapper::from)

            return Clinician(
                clinicianTableRow.email.lowercase(),
                clinicianTableRow.phoneNumber,
                clinicianTableRow.firstName,
                clinicianTableRow.lastName,
                clinicianTableRow.collegeName,
                clinicianTableRow.registrationCode,
                clinicianTableRow.registrationYear,
                clinicianTableRow.type,
                businessProfile,
                availability,
                clinicianTableRow.gender,
                clinicianTableRow.supervisorName
            )
        }
    }
}

class Client(

    email: String,
    phoneNumber: String,
    firstName: String,
    lastName: String,
    auth0UserId: String? = null,

    val gender: UserGender?,
    val birthDate: ZonedDateTime?,
    val familyMembers: List<UserFamilyMember>,

    var clinicianId: String = "",
) : AbstractUser(email, phoneNumber, firstName, lastName, auth0UserId) {

    override fun toEntity(auth0UserId: String): ClientTableRow {
        return ClientTableRow(
            auth0UserId,
            email,
            phoneNumber,
            firstName,
            lastName,
            clinicianId,
            gender,
            birthDate,
            familyMembers.map(UserFamilyMember::toEntity)
        )
    }

    override fun toAuth0User(): User {
        val user = super.toAuth0User()
        user.userMetadata = mapOf(
            "role" to UserRole.CLIENT
        )

        return user
    }

    object Mapper {

        fun from(request: RegisterClientRequest): Client {
            return Client(
                request.email.lowercase(),
                request.phoneNumber,
                request.firstName,
                request.lastName,
                null,
                request.gender,
                request.birthDate,
                request.familyMembers.map(UserFamilyMember.Mapper::from)
            )
        }

        fun from(request: ClientProfileUpdateRequest): Client {
            return Client(
                request.email.lowercase(),
                request.phoneNumber,
                request.firstName,
                request.lastName,
                null,
                request.gender,
                request.birthDate,
                request.familyMembers.map(UserFamilyMember.Mapper::from)
            )
        }

        fun from(clientTableRow: ClientTableRow): Client {
            return Client(
                clientTableRow.email.lowercase(),
                clientTableRow.phoneNumber,
                clientTableRow.firstName,
                clientTableRow.lastName,
                clientTableRow.id,
                clientTableRow.gender,
                clientTableRow.birthDate,
                clientTableRow.familyMembers.map(UserFamilyMember.Mapper::from),
                clientTableRow.clinician
            )
        }
    }
}

enum class ClinicianType {
    THERAPIST,
    DOCTOR
}

enum class UserRole {
    CLINICIAN,
    CLIENT
}

enum class UserGender {
    FEMALE,
    MALE,
    TRANS_FEMALE,
    TRANS_MALE,
    NON_CONFORMING,
    NOT_LISTED,
    NO_ANSWER
}