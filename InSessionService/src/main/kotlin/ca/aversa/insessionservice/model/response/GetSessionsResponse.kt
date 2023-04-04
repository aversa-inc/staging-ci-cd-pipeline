package ca.aversa.insessionservice.model.response

import ca.aversa.insessionservice.model.UserSession

data class GetSessionsResponse(

    val sessions: List<UserSession>
)