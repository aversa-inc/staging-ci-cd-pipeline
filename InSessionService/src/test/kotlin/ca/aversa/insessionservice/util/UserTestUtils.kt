package ca.aversa.insessionservice.util

import ca.aversa.insessionservice.model.Availability
import ca.aversa.insessionservice.model.Client
import ca.aversa.insessionservice.model.Clinician
import ca.aversa.insessionservice.model.ClinicianAvailability
import ca.aversa.insessionservice.model.ClinicianBusinessProfile
import ca.aversa.insessionservice.model.ClinicianType
import ca.aversa.insessionservice.model.Day
import ca.aversa.insessionservice.model.Group
import ca.aversa.insessionservice.model.UserFamilyMember
import ca.aversa.insessionservice.model.UserGender
import ca.aversa.insessionservice.model.entity.GcmConfigDocument
import ca.aversa.insessionservice.model.entity.UserCommonTableRow
import ca.aversa.insessionservice.model.request.ClientProfileUpdateRequest
import ca.aversa.insessionservice.model.request.ClinicianProfileUpdateRequest
import ca.aversa.insessionservice.model.request.ClinicianUpdateAvailabilityRequest
import ca.aversa.insessionservice.model.request.CreateGroupRequest
import ca.aversa.insessionservice.model.request.UpdateClinicianBusinessProfileRequest
import ca.aversa.insessionservice.model.request.UserFamilyMemberRequest
import ca.aversa.insessionservice.util.TestDefaults.TEST_CITY
import ca.aversa.insessionservice.util.TestDefaults.TEST_CLIENT_AUTH0_USER_ID
import ca.aversa.insessionservice.util.TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID
import ca.aversa.insessionservice.util.TestDefaults.TEST_CLINICIAN_TYPE
import ca.aversa.insessionservice.util.TestDefaults.TEST_COLLEGE_NAME
import ca.aversa.insessionservice.util.TestDefaults.TEST_COUNTRY
import ca.aversa.insessionservice.util.TestDefaults.TEST_EMAIL
import ca.aversa.insessionservice.util.TestDefaults.TEST_FIRST_NAME
import ca.aversa.insessionservice.util.TestDefaults.TEST_GCM_TOKEN_ENDPOINT_ARN
import ca.aversa.insessionservice.util.TestDefaults.TEST_GROUP_NAME
import ca.aversa.insessionservice.util.TestDefaults.TEST_LAST_NAME
import ca.aversa.insessionservice.util.TestDefaults.TEST_PHONE_NUMBER
import ca.aversa.insessionservice.util.TestDefaults.TEST_POSTAL_CODE
import ca.aversa.insessionservice.util.TestDefaults.TEST_REGISTRATION_CODE
import ca.aversa.insessionservice.util.TestDefaults.TEST_REGISTRATION_YEAR
import ca.aversa.insessionservice.util.TestDefaults.TEST_STATE
import ca.aversa.insessionservice.util.TestDefaults.TEST_STREET_NAME
import ca.aversa.insessionservice.util.TestDefaults.TEST_STREET_NUMBER
import ca.aversa.insessionservice.util.TestDefaults.TEST_SUPERVISOR_NAME
import ca.aversa.insessionservice.util.TestDefaults.TEST_TAX_REGISTRATION_NUMBER
import java.time.ZonedDateTime

object UserTestUtils {

    fun createSampleClient(
        email: String = TEST_EMAIL,
        phoneNumber: String = TEST_PHONE_NUMBER,
        firstName: String = TEST_FIRST_NAME,
        lastName: String = TEST_LAST_NAME,
        userId: String = TEST_CLIENT_AUTH0_USER_ID,
        clinicianUserId: String = TEST_CLINICIAN_AUTH0_USER_ID,
        gender: UserGender = UserGender.MALE,
        birthDate: ZonedDateTime = ZonedDateTime.now(),
        familyMembers: List<UserFamilyMember> = listOf(RegisterAndLoginTestUtils.createSampleUserFamilyMember())
    ): Client {
        return Client(email, phoneNumber, firstName, lastName, userId, gender, birthDate, familyMembers, clinicianUserId)
    }

    fun createSampleClinician(
        email: String = TEST_EMAIL,
        phoneNumber: String = TEST_PHONE_NUMBER,
        firstName: String = TEST_FIRST_NAME,
        lastName: String = TEST_LAST_NAME,
        collegeName: String = TEST_COLLEGE_NAME,
        registrationCode: String = TEST_REGISTRATION_CODE,
        registrationYear: String = TEST_REGISTRATION_YEAR,
        type: ClinicianType = TEST_CLINICIAN_TYPE,
        businessProfile: ClinicianBusinessProfile? = null,
        availability: ClinicianAvailability? = null,
        gender: UserGender = UserGender.MALE,
        supervisorName: String = TEST_SUPERVISOR_NAME
    ): Clinician {
        return Clinician(
            email, phoneNumber, firstName, lastName,
            collegeName, registrationCode, registrationYear, type, businessProfile, availability,
            gender, supervisorName
        )
    }

    fun createSampleBusinessProfile(
        streetNumber: String = TEST_STREET_NUMBER,
        streetName: String = TEST_STREET_NAME,
        city: String = TEST_CITY,
        state: String = TEST_STATE,
        country: String = TEST_COUNTRY,
        postalCode: String = TEST_POSTAL_CODE,
        taxRegistrationNumber: String = TEST_TAX_REGISTRATION_NUMBER
    ): ClinicianBusinessProfile {
        return ClinicianBusinessProfile(
            streetNumber, streetName, city, state, country, postalCode, taxRegistrationNumber
        )
    }

    fun createSampleClinicianAvailability(): ClinicianAvailability {
        return ClinicianAvailability()
    }

    fun createSampleGroup(
        clinicianId: String = TEST_CLINICIAN_AUTH0_USER_ID,
        clientIds: List<String> = listOf(TEST_CLIENT_AUTH0_USER_ID),
        name: String = TEST_GROUP_NAME
    ): Group {
        return Group(clinicianId, clientIds, name)
    }

    fun createSampleClinicianUpdateAvailabilityRequest(): ClinicianUpdateAvailabilityRequest {
        return ClinicianUpdateAvailabilityRequest(
            mapOf(
                Day.MONDAY to Availability.Default.weekend()
            )
        )
    }

    fun createSampleCreateGroupRequest(
        clientIds: List<String> = listOf(TEST_CLIENT_AUTH0_USER_ID),
        name: String = TEST_GROUP_NAME
    ): CreateGroupRequest {
        return CreateGroupRequest(clientIds, name)
    }

    fun createSampleClinicianUpdateProfileRequest(
        email: String = TEST_EMAIL,
        phoneNumber: String = TEST_PHONE_NUMBER,
        firstName: String = TEST_FIRST_NAME,
        lastName: String = TEST_LAST_NAME,
        collegeName: String = TEST_COLLEGE_NAME,
        registrationCode: String = TEST_REGISTRATION_CODE,
        registrationYear: String = TEST_REGISTRATION_YEAR,
        type: ClinicianType = TEST_CLINICIAN_TYPE
    ): ClinicianProfileUpdateRequest {
        return ClinicianProfileUpdateRequest(
            email,
            phoneNumber,
            firstName,
            lastName,
            collegeName,
            registrationCode,
            registrationYear,
            type
        )
    }

    fun createSampleClientUpdateProfileRequest(
        email: String = TEST_EMAIL,
        phoneNumber: String = TEST_PHONE_NUMBER,
        firstName: String = TEST_FIRST_NAME,
        lastName: String = TEST_LAST_NAME,
        gender: UserGender = UserGender.MALE,
        birthDate: ZonedDateTime = ZonedDateTime.now(),
        familyMembers: List<UserFamilyMemberRequest> = listOf(RegisterAndLoginTestUtils.createSampleUserFamilyMemberRequest())
    ): ClientProfileUpdateRequest {
        return ClientProfileUpdateRequest(email, phoneNumber, firstName, lastName, gender, birthDate, familyMembers)
    }

    fun createSampleUserCommonTableRow(
        auth0UserId: String = TEST_CLINICIAN_AUTH0_USER_ID,
        userFriendlyIdentifier: String = "$TEST_FIRST_NAME $TEST_LAST_NAME",
        gcmConfigs: List<GcmConfigDocument> = listOf(createSampleGcmConfigDocument())
    ): UserCommonTableRow {
        return UserCommonTableRow(auth0UserId, userFriendlyIdentifier, gcmConfigs)
    }

    fun createSampleGcmConfigDocument(gcmEndpointArn: String = TEST_GCM_TOKEN_ENDPOINT_ARN): GcmConfigDocument {
        return GcmConfigDocument(gcmEndpointArn)
    }

    fun createSampleUpdateBusinessProfileRequest(
        streetNumber: String = TEST_STREET_NUMBER,
        streetName: String = TEST_STREET_NAME,
        city: String = TEST_CITY,
        state: String = TEST_STATE,
        country: String = TEST_COUNTRY,
        postalCode: String = TEST_POSTAL_CODE,
        taxRegistrationNumber: String = TEST_TAX_REGISTRATION_NUMBER
    ): UpdateClinicianBusinessProfileRequest {
        return UpdateClinicianBusinessProfileRequest(
            streetNumber, streetName, city, state, country, postalCode, taxRegistrationNumber
        )
    }
}