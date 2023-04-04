package ca.aversa.insessionservice.util

import ca.aversa.insessionservice.model.SessionAttendeeStatus

object PushNotificationUtils {

    private const val NEW_SESSION_CREATION_BODY = "A new session has been scheduled at %s"
    private const val ATTENDEE_UPDATE_STATUS_TITLE = "%s has %s a session"

    fun getNewSessionCreationBody(sessionDate: String): String {
        return NEW_SESSION_CREATION_BODY.format(sessionDate)
    }

    fun getUpdatedAttendeeTitle(userIdentifier: String, status: SessionAttendeeStatus): String {
        return ATTENDEE_UPDATE_STATUS_TITLE.format(
            userIdentifier,
            status.name.lowercase()
        )
    }
}