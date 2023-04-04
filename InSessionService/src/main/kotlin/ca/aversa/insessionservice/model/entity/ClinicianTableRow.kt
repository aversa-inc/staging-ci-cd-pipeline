package ca.aversa.insessionservice.model.entity

import ca.aversa.insessionservice.model.ClinicianType
import ca.aversa.insessionservice.model.UserGender
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter

@DynamoDBDocument
class ClinicianTableRow(

    id: String = "",
    email: String = "",
    phoneNumber: String = "",
    firstName: String = "",
    lastName: String = "",

    @DynamoDBAttribute(attributeName = "college_name")
    var collegeName: String = "",

    @DynamoDBAttribute(attributeName = "registration_code")
    var registrationCode: String = "",

    @DynamoDBAttribute(attributeName = "registration_year")
    var registrationYear: String = "",

    @DynamoDBAttribute(attributeName = "type")
    @DynamoDBTypeConverted(converter = ClinicianTypeConverter::class)
    var type: ClinicianType = ClinicianType.DOCTOR,

    @DynamoDBAttribute(attributeName = "business_profile")
    var businessProfile: ClinicianBusinessProfileDocument? = null,

    @DynamoDBAttribute(attributeName = "availabilities")
    var availabilities: Map<String, AvailabilityDocument> = mutableMapOf(),

    @DynamoDBAttribute(attributeName = "clients")
    var clients: MutableList<String> = mutableListOf(),

    @DynamoDBAttribute(attributeName = "gender")
    @DynamoDBTypeConverted(converter = UserGenderTypeConverter::class)
    var gender: UserGender? = null,

    @DynamoDBAttribute(attributeName = "supervisor_name")
    var supervisorName: String? = null

) : AbstractUserTableRow(id, email, phoneNumber, firstName, lastName) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClinicianTableRow

        if (collegeName != other.collegeName) return false
        if (registrationCode != other.registrationCode) return false
        if (registrationYear != other.registrationYear) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = collegeName.hashCode()
        result = 31 * result + registrationCode.hashCode()
        result = 31 * result + registrationYear.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}

class ClinicianTypeConverter : DynamoDBTypeConverter<String, ClinicianType> {

    override fun convert(clinicianType: ClinicianType?): String {
        return clinicianType!!.name
    }

    override fun unconvert(clinicianType: String?): ClinicianType {
        if (clinicianType == null) {
            throw RuntimeException("Invalid clinician type found in DDB $clinicianType")
        }

        return ClinicianType.valueOf(clinicianType)
    }
}