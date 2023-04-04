package ca.aversa.insessionservice.controller

import ca.aversa.insessionservice.bo.ClientBo
import ca.aversa.insessionservice.bo.ClinicianBo
import ca.aversa.insessionservice.bo.InvoiceBo
import ca.aversa.insessionservice.bo.JournalBo
import ca.aversa.insessionservice.context.RequestAttributeType
import ca.aversa.insessionservice.exception.ClinicianBoException
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.exception.UserProfileUpdateException
import ca.aversa.insessionservice.model.Clinician
import ca.aversa.insessionservice.model.ClinicianAvailability
import ca.aversa.insessionservice.model.ClinicianBusinessProfile
import ca.aversa.insessionservice.model.Group
import ca.aversa.insessionservice.model.Invoice
import ca.aversa.insessionservice.model.Journal
import ca.aversa.insessionservice.util.InvoiceTestUtils
import ca.aversa.insessionservice.util.JournalTestUtils
import ca.aversa.insessionservice.util.TestDefaults
import ca.aversa.insessionservice.util.TestDefaults.TEST_CLIENT_AUTH0_USER_ID
import ca.aversa.insessionservice.util.TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID
import ca.aversa.insessionservice.util.UserTestUtils
import ca.aversa.insessionservice.util.UserTestUtils.createSampleBusinessProfile
import ca.aversa.insessionservice.util.UserTestUtils.createSampleClient
import ca.aversa.insessionservice.util.UserTestUtils.createSampleClinician
import ca.aversa.insessionservice.util.UserTestUtils.createSampleClinicianAvailability
import ca.aversa.insessionservice.util.UserTestUtils.createSampleCreateGroupRequest
import ca.aversa.insessionservice.util.UserTestUtils.createSampleGroup
import ca.aversa.insessionservice.util.UserTestUtils.createSampleUpdateBusinessProfileRequest
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import javax.servlet.http.HttpServletRequest

internal class ClinicianControllerTest {

    private val httpServletRequest: HttpServletRequest = mockk()
    private val clinicianBo: ClinicianBo = mockk()
    private val clientBo: ClientBo = mockk()
    private val journalBo: JournalBo = mockk()
    private val invoiceBo: InvoiceBo = mockk()
    private val clinicianController: ClinicianController = ClinicianController(clinicianBo, clientBo, journalBo, invoiceBo)

    @BeforeEach
    fun setup() {
        every {
            httpServletRequest.getAttribute(RequestAttributeType.CONTEXT.toString())
        }.returns(TestDefaults.TEST_CLINICIAN_CONTEXT)
    }

    @Test
    fun testUpdateProfile_CallClinicianBoToUpdateProfile_ReturnSuccessfulResponse() {
        val request = UserTestUtils.createSampleClinicianUpdateProfileRequest()
        val clinician = Clinician.Mapper.from(request)

        every {
            clinicianBo.updateProfile(clinician, eq(TestDefaults.TEST_CLINICIAN_CONTEXT.auth0UserId))
        }.returns(clinician)

        val response = clinicianController.updateProfile(httpServletRequest, request)

        assertEquals(response.statusCode, HttpStatus.ACCEPTED)
        assertEquals(clinician, response.body!!.user)
    }

    @Test
    fun testUpdateProfile_ClinicianBoThrowsExceptionWithFieldErrors_ReturnFieldErrorsResponse() {
        val request = UserTestUtils.createSampleClinicianUpdateProfileRequest()
        val fieldErrors = mapOf(
            "email" to "incorrect email",
            "phone" to "incorrect phone"
        )

        every {
            clinicianBo.updateProfile(any(), any())
        }.throws(UserProfileUpdateException(fieldErrors = fieldErrors))

        val response = clinicianController.updateProfile(httpServletRequest, request)

        assertEquals(response.statusCode, HttpStatus.CONFLICT)
        assertEquals(response.body!!.fieldErrors, fieldErrors)
    }

    @Test
    fun testGetManagedClients_ClinicianHasCLients_ReturnClientProfiles() {
        val expectedClientIds = listOf(TEST_CLIENT_AUTH0_USER_ID)
        val expectedClients = listOf(createSampleClient())

        every {
            clinicianBo.getManagedClients(TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(expectedClientIds)

        every {
            clientBo.getProfiles(expectedClientIds)
        }.returns(expectedClients)

        val response = clinicianController.getManagedClients(httpServletRequest)

        assertEquals(response.clients, expectedClients)
    }

    @Test
    fun testUpdateAvailability_CallClinicianBoToUpdateAvailability_ReturnSuccessfulResponse() {
        val request = UserTestUtils.createSampleClinicianUpdateAvailabilityRequest()
        val clinicianAvailability = ClinicianAvailability.Mapper.from(request)

        justRun {
            clinicianBo.updateAvailability(TEST_CLINICIAN_AUTH0_USER_ID, clinicianAvailability)
        }

        val response = clinicianController.updateAvailability(httpServletRequest, request)

        assertEquals(response.statusCode, HttpStatus.NO_CONTENT)
    }

    @Test
    fun testUpdateAvailability_ClinicianNotFound_ThrowResourceNotFoundException() {
        val request = UserTestUtils.createSampleClinicianUpdateAvailabilityRequest()
        val clinicianAvailability = ClinicianAvailability.Mapper.from(request)

        every {
            clinicianBo.updateAvailability(TEST_CLINICIAN_AUTH0_USER_ID, clinicianAvailability)
        }.throws(ResourceNotFoundException("Clinician not found"))

        assertThrows<ResourceNotFoundException> {
            clinicianController.updateAvailability(httpServletRequest, request)
        }
    }

    @Test
    fun testGetAvailability_ClinicianFound_ReturnAvailabilities() {
        val clinicianAvailability = createSampleClinicianAvailability()

        every {
            clinicianBo.getAvailability(TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(clinicianAvailability)

        val response = clinicianController.getAvailability(httpServletRequest)

        assertEquals(clinicianAvailability.availabilities, response.availabilities)
    }

    @Test
    fun testGetAvailability_ClinicianNotFound_ThrowResourceNotFoundException() {
        every {
            clinicianBo.getAvailability(TEST_CLINICIAN_AUTH0_USER_ID)
        }.throws(ResourceNotFoundException("Not found"))

        assertThrows<ResourceNotFoundException> {
            clinicianController.getAvailability(httpServletRequest)
        }
    }

    @Test
    fun testCreateGroup_CallClinicianBoToCreateGroup_Successful() {
        val request = createSampleCreateGroupRequest()
        val group = Group.Mapper.from(TEST_CLINICIAN_AUTH0_USER_ID, request)

        justRun {
            clinicianBo.createGroup(group)
        }

        clinicianController.createGroup(httpServletRequest, request)
    }

    @Test
    fun testCreateGroup_ClinicianBoThrowsException_ThrowException() {
        val request = createSampleCreateGroupRequest()
        val group = Group.Mapper.from(TEST_CLINICIAN_AUTH0_USER_ID, request)

        every {
            clinicianBo.createGroup(group)
        }.throws(ClinicianBoException("Somethign broke"))

        assertThrows<ClinicianBoException> {
            clinicianController.createGroup(httpServletRequest, request)
        }
    }

    @Test
    fun testGetGroups_CallClinicianBoToGetGroups_ReturnResults() {
        val expectedGroups = listOf(createSampleGroup())

        every {
            clinicianBo.getGroups(TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(expectedGroups)

        val response = clinicianController.getGroups(httpServletRequest)

        assertEquals(expectedGroups, response.groups)
    }

    @Test
    fun testGetGroups_ClinicianBoThrowsException_ThrowException() {
        every {
            clinicianBo.getGroups(TEST_CLINICIAN_AUTH0_USER_ID)
        }.throws(ClinicianBoException("Something broke"))

        assertThrows<ClinicianBoException> {
            clinicianController.getGroups(httpServletRequest)
        }
    }

    @Test
    fun testUpdateBusinessProfile_CallClinicianBoToUpdateBusinessProfile_Successful() {
        val request = createSampleUpdateBusinessProfileRequest()
        val businessProfile = ClinicianBusinessProfile.Mapper.from(request)

        justRun {
            clinicianBo.updateBusinessProfile(TEST_CLINICIAN_AUTH0_USER_ID, businessProfile)
        }

        val response = clinicianController.updateBusinessProfile(httpServletRequest, request)

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
    }

    @Test
    fun testGetProfile_CallClinicianBoToRetrieveProfile_ReturnClinician() {
        val expectedClinician = createSampleClinician()

        every {
            clinicianBo.getProfile(TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(expectedClinician)

        val actualClinician = clinicianController.getProfile(httpServletRequest)

        assertEquals(expectedClinician.email, actualClinician.email)
        assertEquals(expectedClinician.phoneNumber, actualClinician.phoneNumber)
        assertEquals(expectedClinician.firstName, actualClinician.firstName)
        assertEquals(expectedClinician.lastName, actualClinician.lastName)
        assertEquals(expectedClinician.collegeName, actualClinician.collegeName)
        assertEquals(expectedClinician.registrationCode, actualClinician.registrationCode)
        assertEquals(expectedClinician.registrationYear, actualClinician.registrationYear)
        assertEquals(expectedClinician.type, actualClinician.type)
    }

    @Test
    fun testGetProfile_ClinicianBoThrowsResourceNotFoundException_ThrowResourceNotFoundException() {
        every {
            clinicianBo.getProfile(TEST_CLINICIAN_AUTH0_USER_ID)
        }.throws(ResourceNotFoundException("Clinician not found"))

        assertThrows<ResourceNotFoundException> {
            clinicianController.getProfile(httpServletRequest)
        }
    }

    @Test
    fun testGetBusinessProfile_CallClinicianBoToGetBusinessProfile_ReturnBusinessProfile() {
        val expectedBusinessProfile = createSampleBusinessProfile()

        every {
            clinicianBo.getBusinessProfile(TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(expectedBusinessProfile)

        val response = clinicianController.getBusinessProfile(httpServletRequest)

        assertEquals(expectedBusinessProfile, response.businessProfile)
    }

    @Test
    fun testGetClientJournals_ClinicianRequestedManagedClientsJournals_ReturnClientsJournals() {
        val request = JournalTestUtils.createSampleGetJournalsForUsersRequest()
        val expectedClientIds = listOf(TEST_CLIENT_AUTH0_USER_ID)
        val expectedJournals = listOf(
            JournalTestUtils.createSampleJournal()
        )

        every {
            clinicianBo.getManagedClients(TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(expectedClientIds)

        every {
            journalBo.getAll(TEST_CLIENT_AUTH0_USER_ID, emptyList())
        }.returns(expectedJournals)

        val response = clinicianController.getClientJournals(httpServletRequest, TEST_CLIENT_AUTH0_USER_ID, request)

        assertEquals(expectedJournals.map(Journal::toSimplifiedJournal), response.journals)
    }

    @Test
    fun testGetClientJournals_ClinicianRequestsNonManagedClientsJournals_ThrowResourceNotFoundException() {
        val request = JournalTestUtils.createSampleGetJournalsForUsersRequest()
        val expectedClientIds = listOf(TEST_CLIENT_AUTH0_USER_ID)

        every {
            clinicianBo.getManagedClients(TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(expectedClientIds)

        assertThrows<ResourceNotFoundException> {
            clinicianController.getClientJournals(httpServletRequest, "some-other-client", request)
        }
    }

    @Test
    fun testGetClientJournalDetails_ClinicianRequestedManagedClientsJournals_ReturnClientJournalDetails() {
        val expectedClientIds = listOf(TEST_CLIENT_AUTH0_USER_ID)
        val expectedJournal = JournalTestUtils.createSampleJournal(journalId = "journalId")

        every {
            clinicianBo.getManagedClients(TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(expectedClientIds)

        every {
            journalBo.get(TEST_CLIENT_AUTH0_USER_ID, expectedJournal.id!!)
        }.returns(expectedJournal)

        val actualJournal = clinicianController.getClientJournalDetails(
            httpServletRequest, TEST_CLIENT_AUTH0_USER_ID, expectedJournal.id!!
        )

        assertEquals(expectedJournal, actualJournal)
    }

    @Test
    fun testGetClientJournalDetails_ClinicianRequestedManagedClientsJournals_ThrowResourceNotFoundException() {
        val expectedClientIds = listOf(TEST_CLIENT_AUTH0_USER_ID)
        val expectedJournal = JournalTestUtils.createSampleJournal(journalId = "journalId")

        every {
            clinicianBo.getManagedClients(TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(expectedClientIds)

        every {
            journalBo.get(TEST_CLIENT_AUTH0_USER_ID, expectedJournal.id!!)
        }.throws(ResourceNotFoundException("Not found"))

        assertThrows<ResourceNotFoundException> {
            clinicianController.getClientJournalDetails(
                httpServletRequest, TEST_CLIENT_AUTH0_USER_ID, expectedJournal.id!!
            )
        }
    }

    @Test
    fun testGetClientInvoices_ClinicianRequestedManagedClientsInvoices_ReturnClientsInvoices() {
        val expectedClientIds = listOf(TEST_CLIENT_AUTH0_USER_ID)
        val expectedInvoices = listOf(
            InvoiceTestUtils.createSampleInvoice(TEST_CLIENT_AUTH0_USER_ID),
            InvoiceTestUtils.createSampleInvoice(TEST_CLIENT_AUTH0_USER_ID),
        )

        every {
            clinicianBo.getManagedClients(TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(expectedClientIds)

        every {
            invoiceBo.getInvoicesForUser(TEST_CLIENT_AUTH0_USER_ID)
        }.returns(expectedInvoices)

        val response = clinicianController.getClientInvoices(httpServletRequest, TEST_CLIENT_AUTH0_USER_ID)

        assertEquals(expectedInvoices.map(Invoice::toSimplifiedInvoice), response.invoices)
    }

    @Test
    fun testGetClientInvoices_ClinicianRequestedNonManagedClientsInvoices_ReturnClientsInvoices() {
        val expectedClientIds = listOf(TEST_CLIENT_AUTH0_USER_ID)
        val expectedInvoices = listOf(
            InvoiceTestUtils.createSampleInvoice(TEST_CLIENT_AUTH0_USER_ID),
            InvoiceTestUtils.createSampleInvoice(TEST_CLIENT_AUTH0_USER_ID),
        )

        every {
            clinicianBo.getManagedClients(TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(expectedClientIds)

        every {
            invoiceBo.getInvoicesForUser(TEST_CLIENT_AUTH0_USER_ID)
        }.returns(expectedInvoices)

        assertThrows<ResourceNotFoundException> {
            clinicianController.getClientInvoices(httpServletRequest, "some-other-client")
        }
    }

    @Test
    fun testGetAllClientInvoices_ClinicianRequestedNonManagedClientsInvoices_ReturnAllClientsInvoices() {
        val expectedInvoices = listOf(
            InvoiceTestUtils.createSampleInvoice(TEST_CLIENT_AUTH0_USER_ID),
            InvoiceTestUtils.createSampleInvoice(TEST_CLIENT_AUTH0_USER_ID),
        )

        every {
            invoiceBo.getInvoicesForIssuedByUser(TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(expectedInvoices)

        val response = clinicianController.getAllClientInvoices(httpServletRequest)

        assertEquals(expectedInvoices.map(Invoice::toSimplifiedInvoice), response.invoices)
    }

    @Test
    fun testGetClientInvoiceDetails_ClinicianRequestedManagedClientsInvoiceDetails_ReturnClientsInvoices() {
        val expectedClientIds = listOf(TEST_CLIENT_AUTH0_USER_ID)
        val expectedInvoice = InvoiceTestUtils.createSampleInvoice(TEST_CLIENT_AUTH0_USER_ID)

        every {
            clinicianBo.getManagedClients(TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(expectedClientIds)

        every {
            invoiceBo.getInvoice(TEST_CLIENT_AUTH0_USER_ID, expectedInvoice.invoiceId!!)
        }.returns(expectedInvoice)

        val actualInvoice = clinicianController.getClientInvoiceDetails(
            httpServletRequest, TEST_CLIENT_AUTH0_USER_ID, expectedInvoice.invoiceId!!
        )

        assertEquals(expectedInvoice, actualInvoice)
    }

    @Test
    fun testGetClientInvoiceDetails_ClinicianRequestedNonManagedClientsInvoiceDetails_ThrowResourceNotFoundException() {
        val expectedClientIds = listOf(TEST_CLIENT_AUTH0_USER_ID)
        val expectedInvoice = InvoiceTestUtils.createSampleInvoice(TEST_CLIENT_AUTH0_USER_ID)

        every {
            clinicianBo.getManagedClients(TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(expectedClientIds)

        assertThrows<ResourceNotFoundException> {
            clinicianController.getClientInvoiceDetails(
                httpServletRequest, "some-other-client", expectedInvoice.invoiceId!!
            )
        }
    }

    @Test
    fun testGetClientInvoiceDetails_InvoiceBoThrowsResourceNotFoundException_ThrowResourceNotFoundException() {
        val expectedClientIds = listOf(TEST_CLIENT_AUTH0_USER_ID)

        every {
            clinicianBo.getManagedClients(TEST_CLINICIAN_AUTH0_USER_ID)
        }.returns(expectedClientIds)

        every {
            invoiceBo.getInvoice(TEST_CLIENT_AUTH0_USER_ID, "some-invoice-id")
        }.throws(ResourceNotFoundException("Not found"))

        assertThrows<ResourceNotFoundException> {
            clinicianController.getClientInvoiceDetails(
                httpServletRequest, TEST_CLIENT_AUTH0_USER_ID, "some-invoice-id"
            )
        }
    }
}