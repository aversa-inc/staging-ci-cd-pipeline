package ca.aversa.insessionservice.bo

import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.model.Invoice
import ca.aversa.insessionservice.model.entity.InvoiceTableRow
import ca.aversa.insessionservice.util.Constants
import ca.aversa.insessionservice.util.InvoiceTestUtils
import ca.aversa.insessionservice.util.UserTestUtils
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

internal class InvoiceBoTest {

    private val clinicianBo: ClinicianBo = mockk()
    private val clientBo: ClientBo = mockk()
    private val invoiceDao: BasicDao<InvoiceTableRow> = mockk()
    private val invoiceBo = InvoiceBo(clinicianBo, clientBo, invoiceDao)
    private val invoice = InvoiceTestUtils.createSampleInvoice()
    private val clinician = UserTestUtils.createSampleClinician(
        businessProfile = UserTestUtils.createSampleBusinessProfile()
    )
    private val client = UserTestUtils.createSampleClient()
    private var invoiceId: String = ""

    @BeforeEach
    fun setup() {
        invoiceId = "uuid"

        mockkStatic(UUID::class)

        every {
            UUID.randomUUID().toString()
        }.returns(invoiceId)
    }

    @Test
    fun testCreateInvoice_CreateInvoiceForBusinessProfile_Successful() {
        val tax = invoice.subtotal * Constants.TAX_RATE
        val total = invoice.subtotal + tax
        val invoiceTableRow = invoice.toEntity(invoiceId, clinician.businessProfile!!, tax, total)

        every {
            clientBo.getProfiles(listOf(invoice.userId))
        }.returns(listOf(client))

        every {
            clinicianBo.getProfile(invoice.issuedByUserId)
        }.returns(clinician)

        justRun {
            invoiceDao.update(invoiceTableRow)
        }

        invoiceBo.createInvoice(invoice)
    }

    @Test
    fun testCreateInvoice_ClientNotFound_ThrowResourceNotFoundException() {
        every {
            clientBo.getProfiles(listOf(invoice.userId))
        }.returns(emptyList())

        assertThrows<ResourceNotFoundException> {
            invoiceBo.createInvoice(invoice)
        }
    }

    @Test
    fun testCreateInvoice_BusinessProfileNotFound_ThrowResourceNotFoundException() {
        every {
            clientBo.getProfiles(listOf(invoice.userId))
        }.returns(listOf(client))

        every {
            clinicianBo.getProfile(invoice.issuedByUserId)
        }.throws(ResourceNotFoundException("Clinician not found"))

        assertThrows<ResourceNotFoundException> {
            invoiceBo.createInvoice(invoice)
        }
    }

    @Test
    fun testGetInvoice_InvoiceExistsForUser_ReturnInvoice() {
        val tax = invoice.subtotal * Constants.TAX_RATE
        val total = invoice.subtotal + tax
        val expectedInvoice = invoice.toEntity(invoiceId, clinician.businessProfile!!, tax, total)

        every {
            invoiceDao.get(invoice.userId, invoiceId)
        }.returns(expectedInvoice)

        val actualInvoice = invoiceBo.getInvoice(expectedInvoice.userId, expectedInvoice.invoiceId)

        assertEquals(Invoice.Mapper.from(expectedInvoice), actualInvoice)
    }

    @Test
    fun testGetInvoice_InvoiceDoesNotExistForUser_ThrowResourceNotFoundException() {
        val tax = invoice.subtotal * Constants.TAX_RATE
        val total = invoice.subtotal + tax
        val expectedInvoice = invoice.toEntity(invoiceId, clinician.businessProfile!!, tax, total)

        every {
            invoiceDao.get(invoice.userId, invoiceId)
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            invoiceBo.getInvoice(expectedInvoice.userId, expectedInvoice.invoiceId)
        }
    }

    @Test
    fun testGetInvoicesForUser_FetchInvoicesForUser_ReturnInvoices() {
        val keyConditionExpression = "id = :id"
        val valuesMap = mapOf(
            ":id" to AttributeValue().withS(invoice.userId)
        )
        val expectedInvoices = listOf(
            InvoiceTestUtils.createSampleInvoice(invoice.userId, subtotal = 50.0)
                .toEntity(invoiceId, clinician.businessProfile!!, 10.0, 60.0),
            InvoiceTestUtils.createSampleInvoice(invoice.userId, subtotal = 50.0)
                .toEntity(invoiceId, clinician.businessProfile!!, 10.0, 60.0),
        )

        every {
            invoiceDao.query(keyConditionExpression, valuesMap)
        }.returns(expectedInvoices)

        val actualInvoices = invoiceBo.getInvoicesForUser(invoice.userId)

        assertEquals(expectedInvoices.map(Invoice.Mapper::from), actualInvoices)
    }

    @Test
    fun testGetInvoicesForIssuedByUser_FetchInvoicesForUser_ReturnInvoices() {
        val keyConditionExpression = "issued_by = :issued_by"
        val valuesMap = mapOf(
            ":issued_by" to AttributeValue().withS(invoice.issuedByUserId)
        )
        val expectedInvoices = listOf(
            InvoiceTestUtils.createSampleInvoice(invoice.issuedByUserId, subtotal = 50.0)
                .toEntity(invoiceId, clinician.businessProfile!!, 10.0, 60.0),
            InvoiceTestUtils.createSampleInvoice(invoice.issuedByUserId, subtotal = 50.0)
                .toEntity(invoiceId, clinician.businessProfile!!, 10.0, 60.0),
        )

        every {
            invoiceDao.queryGsi("issued_by_index", keyConditionExpression, valuesMap)
        }.returns(expectedInvoices)

        val actualInvoices = invoiceBo.getInvoicesForIssuedByUser(invoice.issuedByUserId)

        assertEquals(expectedInvoices.map(Invoice.Mapper::from), actualInvoices)
    }
}