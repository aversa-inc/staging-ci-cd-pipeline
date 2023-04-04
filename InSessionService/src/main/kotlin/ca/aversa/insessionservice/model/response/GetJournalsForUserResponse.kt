package ca.aversa.insessionservice.model.response

import ca.aversa.insessionservice.model.SimplifiedJournal

data class GetJournalsForUserResponse(

    val journals: List<SimplifiedJournal>
)