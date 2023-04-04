package ca.aversa.insessionservice.controller

import ca.aversa.insessionservice.bo.InvoiceBo
import ca.aversa.insessionservice.context.RequestAttributeType
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.model.Invoice
import ca.aversa.insessionservice.util.InvoiceTestUtils
import ca.aversa.insessionservice.util.TestDefaults
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.servlet.http.HttpServletRequest

internal class InvoiceControllerTest {

    private val httpServletRequest: HttpServletRequest = mockk()
    private val invoiceBo: InvoiceBo = mockk()
    private val controller = InvoiceController(invoiceBo)

    @BeforeEach
    fun setup() {
        every {
            httpServletRequest.getAttribute(RequestAttributeType.CONTEXT.toString())
        }.returns(TestDefaults.TEST_CLINICIAN_CONTEXT)
    }

    @Test
    fun testCreateInvoice_CallInvoiceBoToCreateInvoice_SuccessfulResponse() {
        val request = InvoiceTestUtils.createSampleCreateInvoiceRequest()
        val invoice = Invoice.Mapper.from(TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID, request)

        justRun {
            invoiceBo.createInvoice(invoice)
        }

        controller.createInvoice(httpServletRequest, request)
    }

    @Test
    fun testCreateInvoice_InvoiceBoThrowsResourceNotFoundException_ThrowResourceNotFoundException() {
        val request = InvoiceTestUtils.createSampleCreateInvoiceRequest()
        val invoice = Invoice.Mapper.from(TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID, request)

        every {
            invoiceBo.createInvoice(invoice)
        }.throws(ResourceNotFoundException("Something wasn't found"))

        assertThrows<ResourceNotFoundException> {
            controller.createInvoice(httpServletRequest, request)
        }
    }

    @Test
    fun testGetInvoiceForLoggedInUser_CallInvoiceBoToGetInvoices_ReturnInvoicesSimplifiedVersion() {
        val expectedInvoices = listOf(
            InvoiceTestUtils.createSampleInvoice(),
            InvoiceTestUtils.createSampleInvoice(),
        )

        every {
            invoiceBo.getInvoicesForUser(TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(expectedInvoices)

        val response = controller.getInvoicesForLoggedInUser(httpServletRequest)

        assertEquals(expectedInvoices.map(Invoice::toSimplifiedInvoice), response.invoices)
    }

    @Test
    fun testGetInvoiceDetailsForLoggedInUser_CallInvoiceBoToGetInvoiceDetails_ReturnInvoice() {
        val expectedInvoice = InvoiceTestUtils.createSampleInvoice(invoiceId = "invoiceId")

        every {
            invoiceBo.getInvoice(TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID, expectedInvoice.invoiceId!!)
        }.returns(expectedInvoice)

        val actualInvoice = controller.getInvoiceDetailsForLoggedInUser(httpServletRequest, expectedInvoice.invoiceId!!)

        assertEquals(expectedInvoice, actualInvoice)
    }
}