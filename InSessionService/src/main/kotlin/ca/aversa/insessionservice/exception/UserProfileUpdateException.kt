package ca.aversa.insessionservice.exception

class UserProfileUpdateException(message: String? = null, cause: Throwable? = null, val fieldErrors: Map<String, String>? = null): RuntimeException(message, cause) {
}