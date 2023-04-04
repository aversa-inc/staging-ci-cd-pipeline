package ca.aversa.insessionservice.util

import ca.aversa.insessionservice.model.Invoice
import ca.aversa.insessionservice.model.request.CreateInvoiceRequest
import java.time.ZonedDateTime

object InvoiceTestUtils {

    fun createSampleInvoice(
        title: String = "Invoice title",
        userId: String = TestDefaults.TEST_CLIENT_AUTH0_USER_ID,
        issuedBy: String = TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID,
        sessionStart: ZonedDateTime = ZonedDateTime.now(),
        sessionEnd: ZonedDateTime = ZonedDateTime.now().plusMinutes(30),
        notes: String = "Invoice notes",
        subtotal: Double = 100.0,
        date: ZonedDateTime = TestDefaults.TEST_INVOICE_DATE,
        invoiceId: String = "invoiceId"
    ): Invoice {
        return Invoice(title, userId, issuedBy, sessionStart, sessionEnd, subtotal, date, notes, invoiceId = invoiceId)
    }

    fun createSampleCreateInvoiceRequest(
        title: String = "Invoice title",
        userId: String = TestDefaults.TEST_CLIENT_AUTH0_USER_ID,
        sessionStart: ZonedDateTime = ZonedDateTime.now(),
        sessionEnd: ZonedDateTime = ZonedDateTime.now().plusMinutes(30),
        notes: String = "Invoice notes",
        subtotal: Double = 100.0,
        date: ZonedDateTime = TestDefaults.TEST_INVOICE_DATE
    ): CreateInvoiceRequest {
        return CreateInvoiceRequest(
            title, userId, sessionStart, sessionEnd, date, notes, subtotal
        )
    }
}