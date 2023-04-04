package ca.aversa.insessionservice.controller

import ca.aversa.insessionservice.bo.UserInvitationBo
import ca.aversa.insessionservice.model.Client
import ca.aversa.insessionservice.model.Clinician
import ca.aversa.insessionservice.model.request.RegisterClientRequest
import ca.aversa.insessionservice.model.request.RegisterClinicianRequest
import ca.aversa.insessionservice.model.request.UserInvitationApproveRequest
import ca.aversa.insessionservice.model.request.UserInvitationRejectRequest
import ca.aversa.insessionservice.model.response.InviteClientResponse
import ca.aversa.insessionservice.model.response.InviteClinicianResponse
import ca.aversa.insessionservice.util.Constants
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping("/invitation")
class UserInvitationController(

    private val invitationBo: UserInvitationBo
): BaseController() {

    @PostMapping("/clinician")
    fun inviteClinician(@Valid @RequestBody request: RegisterClinicianRequest,
                        httpServletRequest: HttpServletRequest): ResponseEntity<InviteClinicianResponse> {
        val clinician = Clinician.Mapper.from(request)

        invitationBo.invite(clinician, "ADMIN")

        return ResponseEntity.ok().body(InviteClinicianResponse())
    }

    @PostMapping("/client")
    fun inviteClient(@Valid @RequestBody request: RegisterClientRequest,
                     httpServletRequest: HttpServletRequest): ResponseEntity<InviteClientResponse> {
        val client = Client.Mapper.from(request)
        val context = super.extractContextFromRequest(httpServletRequest)

        invitationBo.invite(client, context.auth0UserId)

        return ResponseEntity.ok().body(InviteClientResponse())
    }

    @GetMapping("/approve/{id}")
    fun approveInvitation(@Valid request: UserInvitationApproveRequest): RedirectView {
        invitationBo.approve(request.id)

        val redirectView = RedirectView()
        redirectView.url = "${Constants.INVITATION_ACCEPTED_STATIC_LINK}?type=${request.type ?: ""}"

        return redirectView
    }

    @GetMapping("/reject/{id}")
    fun rejectInvitation(@Valid request: UserInvitationRejectRequest): RedirectView {
        invitationBo.reject(request.id)

        val redirectView = RedirectView()
        redirectView.url = Constants.INVITATION_REJECTED_STATIC_LINK

        return redirectView
    }
}
