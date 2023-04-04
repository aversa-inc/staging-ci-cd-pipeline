package ca.aversa.insessionservice.bo

import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.model.ClinicianBusinessProfile
import ca.aversa.insessionservice.model.Invoice
import ca.aversa.insessionservice.model.entity.InvoiceTableRow
import ca.aversa.insessionservice.util.Constants
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import java.util.*

class InvoiceBo(

    private val clinicianBo: ClinicianBo,
    private val clientBo: ClientBo,
    private val invoiceDao: BasicDao<InvoiceTableRow>
) {

    fun createInvoice(invoice: Invoice) {
        if (!doesClientExist(invoice.userId)) {
            throw ResourceNotFoundException("Client ${invoice.userId} does not exist while creating an invoice")
        }

        val tax = invoice.subtotal * Constants.TAX_RATE
        val total = invoice.subtotal + tax
        val businessProfile = getClinicianBusinessProfile(invoice.issuedByUserId)

        val invoiceId = UUID.randomUUID().toString()
        val invoiceTableRow = invoice.toEntity(invoiceId, businessProfile, tax, total)

        invoiceDao.update(invoiceTableRow)
    }

    fun getInvoice(userId: String, invoiceId: String): Invoice {
        val invoice = invoiceDao.get(userId, invoiceId)
            ?: throw ResourceNotFoundException("Invoice not found")

        return Invoice.Mapper.from(invoice)
    }

    fun getInvoicesForUser(userId: String): List<Invoice> {
        val keyConditionExpression = "id = :id"
        val valuesMap = mapOf(
            ":id" to AttributeValue().withS(userId)
        )

        return invoiceDao.query(keyConditionExpression, valuesMap).map(Invoice.Mapper::from)
    }

    fun getInvoicesForIssuedByUser(issuedByUserId: String): List<Invoice> {
        val keyConditionExpression = "issued_by = :issued_by"
        val valuesMap = mutableMapOf<String, AttributeValue>(
            ":issued_by" to AttributeValue().withS(issuedByUserId),
        )

        return invoiceDao.queryGsi("issued_by_index", keyConditionExpression, valuesMap)
            .map(Invoice.Mapper::from)
    }

    private fun getClinicianBusinessProfile(clinicianUserId: String): ClinicianBusinessProfile {
        return clinicianBo.getProfile(clinicianUserId).businessProfile
            ?: throw ResourceNotFoundException("Business profile not found for user $clinicianUserId while creating an invoice")
    }

    private fun doesClientExist(clientUserId: String): Boolean {
        return clientBo.getProfiles(listOf(clientUserId)).isNotEmpty()
    }
}