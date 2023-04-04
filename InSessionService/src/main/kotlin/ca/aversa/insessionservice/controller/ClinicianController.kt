package ca.aversa.insessionservice.controller

import ca.aversa.insessionservice.bo.ClientBo
import ca.aversa.insessionservice.bo.ClinicianBo
import ca.aversa.insessionservice.bo.InvoiceBo
import ca.aversa.insessionservice.bo.JournalBo
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.exception.UserProfileUpdateException
import ca.aversa.insessionservice.model.Client
import ca.aversa.insessionservice.model.Clinician
import ca.aversa.insessionservice.model.ClinicianAvailability
import ca.aversa.insessionservice.model.ClinicianBusinessProfile
import ca.aversa.insessionservice.model.Group
import ca.aversa.insessionservice.model.Invoice
import ca.aversa.insessionservice.model.Journal
import ca.aversa.insessionservice.model.request.ClientProfileUpdateRequest
import ca.aversa.insessionservice.model.request.ClinicianProfileUpdateRequest
import ca.aversa.insessionservice.model.request.ClinicianUpdateAvailabilityRequest
import ca.aversa.insessionservice.model.request.CreateGroupRequest
import ca.aversa.insessionservice.model.request.GetJournalsForUserRequest
import ca.aversa.insessionservice.model.request.UpdateClinicianBusinessProfileRequest
import ca.aversa.insessionservice.model.request.UpdateFcmTokenRequest
import ca.aversa.insessionservice.model.response.GetClinicianAvailabilityResponse
import ca.aversa.insessionservice.model.response.GetClinicianBusinessProfileResponse
import ca.aversa.insessionservice.model.response.GetClinicianFullProfileResponse
import ca.aversa.insessionservice.model.response.GetClinicianGroupsResponse
import ca.aversa.insessionservice.model.response.GetInvoicesResponse
import ca.aversa.insessionservice.model.response.GetJournalsForUserResponse
import ca.aversa.insessionservice.model.response.GetManagedClientsResponse
import ca.aversa.insessionservice.model.response.UserProfileUpdateResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@RestController
@RequestMapping("/clinician")
class ClinicianController(

    private val clinicianBo: ClinicianBo,
    private val clientBo: ClientBo,
    private val journalBo: JournalBo,
    private val invoiceBo: InvoiceBo
) : BaseController() {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @PutMapping
    fun updateProfile(
        httpServletRequest: HttpServletRequest,
        @Valid @RequestBody request: ClinicianProfileUpdateRequest
    ): ResponseEntity<UserProfileUpdateResponse> {
        val context = super.extractContextFromRequest(httpServletRequest)
        val clinician = Clinician.Mapper.from(request)

        return try {
            clinicianBo.updateProfile(clinician, context.auth0UserId)

            ResponseEntity.status(HttpStatus.ACCEPTED).body(UserProfileUpdateResponse(user = clinician))
        } catch (e: UserProfileUpdateException) {
            if (e.fieldErrors != null) {
                ResponseEntity.status(HttpStatus.CONFLICT).body(UserProfileUpdateResponse(fieldErrors = e.fieldErrors))
            } else {
                throw e
            }
        }
    }

    @GetMapping
    fun getProfile(httpServletRequest: HttpServletRequest): GetClinicianFullProfileResponse {
        val context = super.extractContextFromRequest(httpServletRequest)

        val clinician = clinicianBo.getProfile(context.auth0UserId)

        return GetClinicianFullProfileResponse(
            clinician.email,
            clinician.phoneNumber,
            clinician.firstName,
            clinician.lastName,
            clinician.collegeName,
            clinician.registrationCode,
            clinician.registrationYear,
            clinician.type,
            clinician.gender,
            clinician.supervisorName
        )
    }

    @PutMapping("/business-profile")
    fun updateBusinessProfile(
        httpServletRequest: HttpServletRequest,
        @Valid @RequestBody request: UpdateClinicianBusinessProfileRequest
    ): ResponseEntity<Void> {
        val context = super.extractContextFromRequest(httpServletRequest)
        val businessProfile = ClinicianBusinessProfile.Mapper.from(request)

        clinicianBo.updateBusinessProfile(context.auth0UserId, businessProfile)

        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }

    @GetMapping("/business-profile")
    fun getBusinessProfile(httpServletRequest: HttpServletRequest): GetClinicianBusinessProfileResponse {
        val context = super.extractContextFromRequest(httpServletRequest)

        val businessProfile = clinicianBo.getBusinessProfile(context.auth0UserId)

        return GetClinicianBusinessProfileResponse(businessProfile)
    }

    @GetMapping("/clients")
    fun getManagedClients(httpServletRequest: HttpServletRequest): GetManagedClientsResponse {
        val context = super.extractContextFromRequest(httpServletRequest)

        val clientIds = clinicianBo.getManagedClients(context.auth0UserId)
        val clients = clientBo.getProfiles(clientIds)

        return GetManagedClientsResponse(clients)
    }

    @PatchMapping("/device-token")
    fun updateDeviceToken(
        httpServletRequest: HttpServletRequest,
        @Valid @RequestBody request: UpdateFcmTokenRequest
    ): ResponseEntity<Void> {
        val context = super.extractContextFromRequest(httpServletRequest)

        clinicianBo.updateFcmToken(context.auth0UserId, request.fcmToken)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PatchMapping("/availability")
    fun updateAvailability(
        httpServletRequest: HttpServletRequest,
        @Valid @RequestBody request: ClinicianUpdateAvailabilityRequest
    ): ResponseEntity<Void> {
        val context = super.extractContextFromRequest(httpServletRequest)
        val clinicianAvailability = ClinicianAvailability.Mapper.from(request)

        clinicianBo.updateAvailability(context.auth0UserId, clinicianAvailability)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @GetMapping("/availability")
    fun getAvailability(httpServletRequest: HttpServletRequest): GetClinicianAvailabilityResponse {
        val context = super.extractContextFromRequest(httpServletRequest)

        val availabilities = clinicianBo.getAvailability(context.auth0UserId).availabilities

        return GetClinicianAvailabilityResponse(availabilities)
    }

    @PostMapping("/groups")
    fun createGroup(
        httpServletRequest: HttpServletRequest,
        @Valid @RequestBody request: CreateGroupRequest
    ): ResponseEntity<Void> {
        val context = super.extractContextFromRequest(httpServletRequest)
        val group = Group.Mapper.from(context.auth0UserId, request)

        clinicianBo.createGroup(group)

        return ResponseEntity.ok().build()
    }

    @GetMapping("/groups")
    fun getGroups(httpServletRequest: HttpServletRequest): GetClinicianGroupsResponse {
        val context = super.extractContextFromRequest(httpServletRequest)

        val groups = clinicianBo.getGroups(context.auth0UserId)

        return GetClinicianGroupsResponse(groups)
    }

    @PutMapping("/client/{clientId}")
    fun updateClientProfile(
        httpServletRequest: HttpServletRequest,
        @PathVariable @Pattern(regexp = "^.{28}$", message = "Please provide a valid client Id") clientId: String,
        @Valid @RequestBody request: ClientProfileUpdateRequest
    ): ResponseEntity<UserProfileUpdateResponse> {
        // Ensure only the clinician can update their own managed client
        val context = super.extractContextFromRequest(httpServletRequest)

        validateClinicianAccessForClient(context.auth0UserId, clientId)

        return try {
            val client = Client.Mapper.from(request)

            clientBo.updateProfile(client, clientId)

            ResponseEntity.status(HttpStatus.ACCEPTED).body(UserProfileUpdateResponse(user = client))
        } catch (e: UserProfileUpdateException) {
            if (e.fieldErrors != null) {
                ResponseEntity.status(HttpStatus.CONFLICT).body(UserProfileUpdateResponse(fieldErrors = e.fieldErrors))
            } else {
                throw e
            }
        }
    }

    @GetMapping("/client/{clientId}/journals")
    fun getClientJournals(
        httpServletRequest: HttpServletRequest,
        @PathVariable @Pattern(regexp = "^.{28}$", message = "Please provide a valid client Id") clientId: String,
        @Valid request: GetJournalsForUserRequest
    ): GetJournalsForUserResponse {
        // Ensure only the clinician can see their own client's journals
        val context = super.extractContextFromRequest(httpServletRequest)

        validateClinicianAccessForClient(context.auth0UserId, clientId)

        val keywords = request.keywords?.split(",")?.toList() ?: emptyList()
        val journals = journalBo.getAll(clientId, keywords).map(Journal::toSimplifiedJournal)

        return GetJournalsForUserResponse(journals)
    }

    @GetMapping("/client/{clientId}/journals/{journalId}")
    fun getClientJournalDetails(
        httpServletRequest: HttpServletRequest,
        @PathVariable @Pattern(regexp = "^.{28}$", message = "Please provide a valid client Id") clientId: String,
        @PathVariable @Size(min = 5, max = 35, message = "Invalid journal identifier provided") journalId: String,
    ): Journal {
        // Ensure only the clinician can see their own client's journals
        val context = super.extractContextFromRequest(httpServletRequest)

        validateClinicianAccessForClient(context.auth0UserId, clientId)

        return journalBo.get(clientId, journalId)
    }

    @GetMapping("/client/invoice")
    fun getAllClientInvoices(httpServletRequest: HttpServletRequest): GetInvoicesResponse {
        // Ensure only the clinician can see their own client's journals
        val context = super.extractContextFromRequest(httpServletRequest)

        val invoices = invoiceBo.getInvoicesForIssuedByUser(context.auth0UserId).map(Invoice::toSimplifiedInvoice)

        return GetInvoicesResponse(invoices)
    }

    @GetMapping("/client/{clientId}/invoice")
    fun getClientInvoices(
        httpServletRequest: HttpServletRequest,
        @PathVariable @Pattern(regexp = "^.{28}$", message = "Please provide a valid client Id") clientId: String,
    ): GetInvoicesResponse {
        // Ensure only the clinician can see their own client's journals
        val context = super.extractContextFromRequest(httpServletRequest)

        validateClinicianAccessForClient(context.auth0UserId, clientId)

        val invoices = invoiceBo.getInvoicesForUser(clientId).map(Invoice::toSimplifiedInvoice)

        return GetInvoicesResponse(invoices)
    }

    @GetMapping("/client/{clientId}/invoice/{invoiceId}")
    fun getClientInvoiceDetails(
        httpServletRequest: HttpServletRequest,
        @PathVariable @Pattern(regexp = "^.{28}$", message = "Please provide a valid client Id") clientId: String,
        @PathVariable @Size(min = 36, max = 36, message = "Invalid identifier provided") invoiceId: String
    ): Invoice {
        // Ensure only the clinician can see their own client's journals
        val context = super.extractContextFromRequest(httpServletRequest)

        validateClinicianAccessForClient(context.auth0UserId, clientId)

        return invoiceBo.getInvoice(clientId, invoiceId)
    }

    private fun validateClinicianAccessForClient(clinicianId: String, clientId: String) {
        val clientIds = clinicianBo.getManagedClients(clinicianId)

        if (!clientIds.contains(clientId)) {
            throw ResourceNotFoundException("Clinician does not manage client $clientId")
        }
    }
}
