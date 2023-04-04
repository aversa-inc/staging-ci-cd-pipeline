package ca.aversa.insessionservice.util

object Constants {

    const val INVITATION_ACCEPTED_STATIC_LINK = "https://aversa.ca/invitation-accepted.html"
    const val INVITATION_REJECTED_STATIC_LINK = "https://aversa.ca/invitation-rejected.html"
    const val CLINICIAN_DAO_BEAN_QUALIFIER = "CLINICIAN_DAO_BEAN_QUALIFIER"
    const val CLIENT_DAO_BEAN_QUALIFIER = "CLIENT_DAO_BEAN_QUALIFIER"
    const val USER_INVITATION_DAO_BEAN_QUALIFIER = "USER_INVITATION_DAO_BEAN_QUALIFIER"
    const val BOOKED_SESSIONS_DAO_BEAN_QUALIFIER = "BOOKED_SESSIONS_DAO_BEAN_QUALIFIER"
    const val BOOKED_SESSIONS_ASSOCIATION_DAO_BEAN_QUALIFIER = "BOOKED_SESSIONS_ASSOCIATIONS_DAO_BEAN_QUALIFIER"
    const val USER_COMMON_DAO_BEAN_QUALIFIER = "USER_COMMON_DAO_BEAN_QUALIFIER"
    const val USER_GROUP_DAO_BEAN_QUALIFIER = "USER_GROUP_DAO_BEAN_QUALIFIER"
    const val USER_JOURNAL_DAO_BEAN_QUALIFIER = "USER_JOURNAL_DAO_BEAN_QUALIFIER"
    const val INVOICE_DAO_BEAN_QUALIFIER = "INVOICE_DAO_BEAN_QUALIFIER"

    // Standard ON tax rate until a more scalable/generic solution is in place
    const val TAX_RATE: Double = 0.13

    // Test account info for Apple and Google to approve app during review
    const val TEST_APPSTORE_CLINICIAN_EMAIL = "aversacliniciantest@gmail.com"
    const val TEST_APPSTORE_CLIENT_EMAIL = "aversaclienttest@gmail.com"
    const val TEST_APPSTORE_CLINICIAN_PHONE = "+11111111111"
    const val TEST_APPSTORE_CLIENT_PHONE = "+12222222222"
}
