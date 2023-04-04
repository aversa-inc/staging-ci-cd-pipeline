package ca.aversa.insessionservice.util

import ca.aversa.insessionservice.util.TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID
import ca.aversa.insessionservice.util.TestDefaults.TEST_EMAIL
import ca.aversa.insessionservice.util.TestDefaults.TEST_FIRST_NAME
import ca.aversa.insessionservice.util.TestDefaults.TEST_PHONE_NUMBER
import ca.aversa.insessionservice.util.TestDefaults.TEST_USER_ROLE
import com.auth0.json.mgmt.users.User

object Auth0TestUtils {

    fun createSampleAuth0User(isPhoneVerified: Boolean = false, isBlocked: Boolean = true): User {
        val user = User()
        user.id = TEST_CLINICIAN_AUTH0_USER_ID
        user.name = TEST_FIRST_NAME
        user.phoneNumber = TEST_PHONE_NUMBER
        user.email = TEST_EMAIL
        user.isPhoneVerified = isPhoneVerified
        user.isBlocked = isBlocked
        user.userMetadata = mapOf(
            "role" to TEST_USER_ROLE
        )

        return user
    }

    fun createSampleAuth0UserAddResponse(isPhoneVerified: Boolean = false): User {
        val user = createSampleAuth0User(isPhoneVerified)
        user.id = TEST_CLINICIAN_AUTH0_USER_ID

        return user
    }
}