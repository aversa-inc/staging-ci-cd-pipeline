package ca.aversa.insessionservice.util

import ca.aversa.insessionservice.model.ClinicianType
import ca.aversa.insessionservice.model.UserFamilyMember
import ca.aversa.insessionservice.model.UserGender
import ca.aversa.insessionservice.model.UserRole
import ca.aversa.insessionservice.model.entity.UserInvitationTableRow
import ca.aversa.insessionservice.model.request.PasswordlessRequest
import ca.aversa.insessionservice.model.request.RegisterClientRequest
import ca.aversa.insessionservice.model.request.RegisterClinicianRequest
import ca.aversa.insessionservice.model.request.UserFamilyMemberRequest
import ca.aversa.insessionservice.model.request.UserInvitationApproveRequest
import ca.aversa.insessionservice.model.request.UserInvitationRejectRequest
import ca.aversa.insessionservice.util.TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID
import ca.aversa.insessionservice.util.TestDefaults.TEST_CLINICIAN_TYPE
import ca.aversa.insessionservice.util.TestDefaults.TEST_COLLEGE_NAME
import ca.aversa.insessionservice.util.TestDefaults.TEST_EMAIL
import ca.aversa.insessionservice.util.TestDefaults.TEST_FIRST_NAME
import ca.aversa.insessionservice.util.TestDefaults.TEST_LAST_NAME
import ca.aversa.insessionservice.util.TestDefaults.TEST_PHONE_NUMBER
import ca.aversa.insessionservice.util.TestDefaults.TEST_REGISTRATION_CODE
import ca.aversa.insessionservice.util.TestDefaults.TEST_REGISTRATION_YEAR
import ca.aversa.insessionservice.util.TestDefaults.TEST_SMS_GATEWAY_BODY
import ca.aversa.insessionservice.util.TestDefaults.TEST_USER_INVITATION_ID
import ca.aversa.insessionservice.util.TestDefaults.TEST_USER_INVITATION_USER_PAYLOAD
import ca.aversa.insessionservice.util.TestDefaults.TEST_USER_ROLE
import java.time.ZonedDateTime

object RegisterAndLoginTestUtils {

    fun createSampleRegisterClinicianRequest(
        email: String = TEST_EMAIL,
        phoneNumber: String = TEST_PHONE_NUMBER,
        firstName: String = TEST_FIRST_NAME,
        lastName: String = TEST_LAST_NAME,
        collegeName: String = TEST_COLLEGE_NAME,
        registrationCode: String = TEST_REGISTRATION_CODE,
        registrationYear: String = TEST_REGISTRATION_YEAR,
        type: ClinicianType = TEST_CLINICIAN_TYPE
    ): RegisterClinicianRequest {
        return RegisterClinicianRequest(
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

    fun createSampleRegisterClientRequest(
        email: String = TEST_EMAIL,
        phoneNumber: String = TEST_PHONE_NUMBER,
        firstName: String = TEST_FIRST_NAME,
        lastName: String = TEST_LAST_NAME,
        gender: UserGender = UserGender.MALE,
        birthDate: ZonedDateTime = ZonedDateTime.now(),
        familyMembers: List<UserFamilyMemberRequest> = listOf(createSampleUserFamilyMemberRequest())
    ): RegisterClientRequest {
        return RegisterClientRequest(
            email,
            phoneNumber,
            firstName,
            lastName,
            gender,
            birthDate,
            familyMembers
        )
    }

    fun createSampleUserFamilyMemberRequest(
        email: String = TEST_EMAIL,
        phoneNumber: String = TEST_PHONE_NUMBER,
        firstName: String = TEST_FIRST_NAME,
        lastName: String = TEST_LAST_NAME,
        isEmergencyContact: Boolean = false
    ): UserFamilyMemberRequest {
        return UserFamilyMemberRequest(email, phoneNumber, firstName, lastName, isEmergencyContact)
    }

    fun createSampleUserFamilyMember(
        email: String = TEST_EMAIL,
        phoneNumber: String = TEST_PHONE_NUMBER,
        firstName: String = TEST_FIRST_NAME,
        lastName: String = TEST_LAST_NAME,
        isEmergencyContact: Boolean = false
    ): UserFamilyMember {
        return UserFamilyMember(email, phoneNumber, firstName, lastName, isEmergencyContact)
    }

    fun createSampleUserInvitationApproveRequest(): UserInvitationApproveRequest {
        return UserInvitationApproveRequest(TEST_USER_INVITATION_ID)
    }

    fun createSampleUserInvitationRejectRequest(): UserInvitationRejectRequest {
        return UserInvitationRejectRequest(TEST_USER_INVITATION_ID)
    }

    fun createSamplePasswordlessRequest(
        phoneNumber: String = TEST_PHONE_NUMBER,
        body: String = TEST_SMS_GATEWAY_BODY
    ): PasswordlessRequest {
        return PasswordlessRequest(phoneNumber, body)
    }

    fun createUserSampleUserInvitationTableRow(
        id: String = TEST_USER_INVITATION_ID,
        userRole: UserRole = TEST_USER_ROLE,
        auth0UserId: String = TEST_CLINICIAN_AUTH0_USER_ID,
        userPayload: String = TEST_USER_INVITATION_USER_PAYLOAD
    ): UserInvitationTableRow {
        return UserInvitationTableRow(
            id,
            userRole,
            auth0UserId,
            userPayload,
            "referredById"
        )
    }
}