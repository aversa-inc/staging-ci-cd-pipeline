package ca.aversa.insessionservice.util

object ResponseConstants {

    const val DUPLICATE_RESOURCE_EXCEPTION_MESSAGE = "Resource with identifier already exists"
    const val SESSION_TIME_CONFLICT_EXCEPTION = "Clinician already has a session booked at this time"
    const val GENERAL_EXCEPTION_MESSAGE = "We're having some difficulty processing your request right now. Please try again later"
    const val RESOURCE_NOT_FOUND_MESSAGE = "Resource with the given identifier not found"
}