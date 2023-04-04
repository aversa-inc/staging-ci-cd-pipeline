package ca.aversa.insessionservice.controller

import ca.aversa.insessionservice.bo.ClientBo
import ca.aversa.insessionservice.bo.ClinicianBo
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.exception.UserProfileUpdateException
import ca.aversa.insessionservice.model.Client
import ca.aversa.insessionservice.model.request.ClientProfileUpdateRequest
import ca.aversa.insessionservice.model.request.UpdateFcmTokenRequest
import ca.aversa.insessionservice.model.response.GetClinicianSimpleProfileResponse
import ca.aversa.insessionservice.model.response.UserProfileUpdateResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping("/client")
class ClientController(

    private val clientBo: ClientBo,
    private val clinicianBo: ClinicianBo
): BaseController() {

    @PutMapping
    fun updateProfile(httpServletRequest: HttpServletRequest,
                      @Valid @RequestBody request: ClientProfileUpdateRequest
    ): ResponseEntity<UserProfileUpdateResponse> {
        val context = super.extractContextFromRequest(httpServletRequest)
        val client = Client.Mapper.from(request)

        return try {
            clientBo.updateProfile(client, context.auth0UserId)

            ResponseEntity.status(HttpStatus.ACCEPTED).body(UserProfileUpdateResponse(user = client))
        }
        catch(e: UserProfileUpdateException) {
            if(e.fieldErrors != null) {
                ResponseEntity.status(HttpStatus.CONFLICT).body(UserProfileUpdateResponse(fieldErrors = e.fieldErrors))
            }
            else {
                throw e
            }
        }
    }

    @GetMapping
    fun getProfile(httpServletRequest: HttpServletRequest): Client {
        val context = super.extractContextFromRequest(httpServletRequest)

        val client = clientBo.getProfiles(listOf(context.auth0UserId))[0]

        return Client(
            client.email,
            client.phoneNumber,
            client.firstName,
            client.lastName,
            clinicianId = client.clinicianId,
            gender = client.gender,
            birthDate = client.birthDate,
            familyMembers = client.familyMembers
        )
    }

    @GetMapping("/clinician")
    fun getClinicianProfile(httpServletRequest: HttpServletRequest): GetClinicianSimpleProfileResponse {
        val context = super.extractContextFromRequest(httpServletRequest)

        val clients = clientBo.getProfiles(listOf(context.auth0UserId))

        if(clients.isEmpty()) {
            throw ResourceNotFoundException("Client not found")
        }

        val clinician = clinicianBo.getProfile(clients[0].clinicianId)

        return GetClinicianSimpleProfileResponse(
            clinician.firstName,
            clinician.lastName,
            clinician.type
        )
    }

    @PatchMapping("/device-token")
    fun updateDeviceToken(httpServletRequest: HttpServletRequest,
                          @Valid @RequestBody request: UpdateFcmTokenRequest): ResponseEntity<Void> {
        val context = super.extractContextFromRequest(httpServletRequest)

        clientBo.updateFcmToken(context.auth0UserId, request.fcmToken)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}