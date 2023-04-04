package ca.aversa.insessionservice.context

import ca.aversa.insessionservice.model.UserRole

class Context(

    val role: UserRole,
    val auth0UserId: String
) {
    override fun toString(): String {
        return "Context(role=$role, auth0UserId='$auth0UserId')"
    }
}

enum class RequestAttributeType(s: String) {
    CONTEXT("context")
}