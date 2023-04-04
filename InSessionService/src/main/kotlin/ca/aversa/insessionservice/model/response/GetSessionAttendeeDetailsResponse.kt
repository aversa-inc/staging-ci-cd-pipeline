package ca.aversa.insessionservice.model.response

import ca.aversa.insessionservice.model.SessionAttendeeDetail

class GetSessionAttendeeDetailsResponse(

    val attendees: Map<String, SessionAttendeeDetail>
)