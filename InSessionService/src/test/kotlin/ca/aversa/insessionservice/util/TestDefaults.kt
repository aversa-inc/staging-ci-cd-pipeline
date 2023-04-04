package ca.aversa.insessionservice.util

import ca.aversa.insessionservice.context.Context
import ca.aversa.insessionservice.model.ClinicianType
import ca.aversa.insessionservice.model.UserRole
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

object TestDefaults {

    const val TEST_EMAIL = "testemail@test.com"
    const val TEST_FIRST_NAME = "firstname"
    const val TEST_LAST_NAME = "lastname"
    const val TEST_PHONE_NUMBER = "3333333333"
    const val TEST_COLLEGE_NAME = "SOme college"
    const val TEST_REGISTRATION_CODE = "111aaaaZZZ"
    const val TEST_REGISTRATION_YEAR = "2022"
    const val TEST_SMS_GATEWAY_BODY = "Your code is something and send it yoo"
    const val TEST_CLINICIAN_AUTH0_USER_ID = "clinician-auth0-user-id"
    const val TEST_CLIENT_AUTH0_USER_ID = "client-auth0-user-id"
    const val TEST_USER_INVITATION_ID = "invitation-id"
    const val TEST_USER_INVITATION_USER_PAYLOAD = "user-payload"
    const val TEST_ROOM_ID = "someroomid"
    const val TEST_GROUP_NAME = "somegroupname"
    const val TEST_GCM_TOKEN = "someGcmToken"
    const val TEST_GCM_TOKEN_ENDPOINT_ARN = "arn:aws/someendpointarn"
    const val TEST_JOURNAL_TITLE = "sometitle"
    const val TEST_JOURNAL_TEXT = "sometext"
    const val TEST_TAX_REGISTRATION_NUMBER = "taxregistrationnumber"
    const val TEST_STREET_NUMBER = "1"
    const val TEST_STREET_NAME = "main street"
    const val TEST_CITY = "Toronto"
    const val TEST_STATE = "ON"
    const val TEST_COUNTRY = "CA"
    const val TEST_POSTAL_CODE = "A1A1A1"
    const val TEST_SUPERVISOR_NAME = "supervisorname"
    val TEST_INVOICE_DATE = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("UTC"))
    val TEST_JOURNAL_KEYWORDS = listOf("somekeyword", "client1", "client2")
    val TEST_JOURNAL_DATE: ZonedDateTime = ZonedDateTime.of(LocalDateTime.of(2022, 1, 1, 0, 0, 0), ZoneId.of("UTC"))
    val TEST_USER_ROLE = UserRole.CLINICIAN
    val TEST_CLINICIAN_TYPE = ClinicianType.DOCTOR
    val TEST_CLINICIAN_CONTEXT = Context(UserRole.CLINICIAN, TEST_CLINICIAN_AUTH0_USER_ID)
    val TEST_CLIENT_CONTEXT = Context(UserRole.CLIENT, TEST_CLIENT_AUTH0_USER_ID)
}