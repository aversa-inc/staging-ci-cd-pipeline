package ca.aversa.insessionservice.model

import ca.aversa.insessionservice.model.entity.InvoiceTableRow
import ca.aversa.insessionservice.model.request.CreateInvoiceRequest
import java.time.ZonedDateTime

data class Invoice(

    val title: String,
    val userId: String,
    val issuedByUserId: String,
    val sessionStart: ZonedDateTime,
    val sessionEnd: ZonedDateTime,
    val subtotal: Double,
    val date: ZonedDateTime,
    val notes: String?,
    val tax: Double? = null,
    val total: Double? = null,
    val invoiceId: String? = null
) {
    fun toEntity(invoiceId: String, businessProfile: ClinicianBusinessProfile, tax: Double, total: Double): InvoiceTableRow {
        return InvoiceTableRow(
            userId,
            invoiceId,
            issuedByUserId,
            date,
            sessionStart,
            sessionEnd,
            businessProfile.toEntity(),
            title,
            notes,
            subtotal,
            tax,
            total
        )
    }

    fun toSimplifiedInvoice(): SimplifiedInvoice {
        return SimplifiedInvoice(
            title,
            userId,
            invoiceId ?: "",
            date,
            total ?: 0.0
        )
    }

    object Mapper {
        fun from(issuedByUserId: String, request: CreateInvoiceRequest): Invoice {
            return Invoice(
                request.title,
                request.userId,
                issuedByUserId,
                request.sessionStart,
                request.sessionEnd,
                request.subtotal,
                request.date,
                request.notes,
            )
        }

        fun from(row: InvoiceTableRow): Invoice {
            return Invoice(
                row.title,
                row.userId,
                row.issuedByUserId,
                row.sessionStartTime,
                row.sessionEndTime,
                row.subtotal,
                row.date,
                row.notes,
                row.tax,
                row.total,
                row.invoiceId
            )
        }
    }
}

data class SimplifiedInvoice(

    val title: String,
    val userId: String,
    val invoiceId: String,
    val date: ZonedDateTime,
    val total: Double
)
