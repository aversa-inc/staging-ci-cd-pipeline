package ca.aversa.insessionservice.model.response

import ca.aversa.insessionservice.model.Client

data class GetManagedClientsResponse(

    val clients: List<Client>
)