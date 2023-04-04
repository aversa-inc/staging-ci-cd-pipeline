package ca.aversa.insessionservice.model.response

import ca.aversa.insessionservice.model.SimplifiedInvoice

data class GetInvoicesResponse(

    val invoices: List<SimplifiedInvoice>
)