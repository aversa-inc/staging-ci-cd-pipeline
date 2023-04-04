package ca.aversa.insessionservice.controller

import ca.aversa.insessionservice.bo.InvoiceBo
import ca.aversa.insessionservice.model.Invoice
import ca.aversa.insessionservice.model.request.CreateInvoiceRequest
import ca.aversa.insessionservice.model.response.GetInvoicesResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.constraints.Size

@RestController
@RequestMapping("/invoice")
class InvoiceController(

    private val invoiceBo: InvoiceBo
) : BaseController() {

    @PostMapping
    fun createInvoice(
        httpServletRequest: HttpServletRequest,
        @Valid @RequestBody request: CreateInvoiceRequest
    ): ResponseEntity<Void> {
        val context = super.extractContextFromRequest(httpServletRequest)
        val invoice = Invoice.Mapper.from(context.auth0UserId, request)

        invoiceBo.createInvoice(invoice)

        return ResponseEntity.ok().build()
    }

    @GetMapping
    fun getInvoicesForLoggedInUser(httpServletRequest: HttpServletRequest): GetInvoicesResponse {
        val context = super.extractContextFromRequest(httpServletRequest)

        val invoices = invoiceBo.getInvoicesForUser(context.auth0UserId).map(Invoice::toSimplifiedInvoice)

        return GetInvoicesResponse(invoices)
    }

    @GetMapping("/{invoiceId}")
    fun getInvoiceDetailsForLoggedInUser(
        httpServletRequest: HttpServletRequest,
        @PathVariable @Size(min = 36, max = 36, message = "Invalid identifier provided") invoiceId: String
    ): Invoice {
        val context = super.extractContextFromRequest(httpServletRequest)

        return invoiceBo.getInvoice(context.auth0UserId, invoiceId)
    }
}