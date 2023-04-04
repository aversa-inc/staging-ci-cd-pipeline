package ca.aversa.insessionservice.controller

import ca.aversa.insessionservice.bo.SessionBo
import ca.aversa.insessionservice.model.Session
import ca.aversa.insessionservice.model.SessionAttendee
import ca.aversa.insessionservice.model.UserSession
import ca.aversa.insessionservice.model.request.CreateSessionRequest
import ca.aversa.insessionservice.model.request.UpdateSessionAttendee
import ca.aversa.insessionservice.model.response.GetSessionAttendeeDetailsResponse
import ca.aversa.insessionservice.model.response.GetSessionsResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.constraints.Size

@RestController
@RequestMapping("/sessions")
class SessionController(

    private val sessionBo: SessionBo
) : BaseController() {

    @PostMapping
    fun createSessions(
        httpServletRequest: HttpServletRequest,
        @Valid @RequestBody request: CreateSessionRequest
    ): ResponseEntity<Void> {
        val context = super.extractContextFromRequest(httpServletRequest)

        sessionBo.createSessions(Session.Mapper.from(request), context.auth0UserId)

        return ResponseEntity.ok().build()
    }

    @GetMapping
    fun getSessions(httpServletRequest: HttpServletRequest): GetSessionsResponse {
        val context = super.extractContextFromRequest(httpServletRequest)

        val sessions = sessionBo.getSessions(context.auth0UserId)

        return GetSessionsResponse(sessions)
    }

    @GetMapping("/{id}")
    fun getSession(
        httpServletRequest: HttpServletRequest,
        @PathVariable @Size(min = 5, max = 35, message = "Invalid identifier provided") id: String
    ): UserSession {
        val context = super.extractContextFromRequest(httpServletRequest)

        return sessionBo.getSession(context.auth0UserId, id)
    }

    @PutMapping("/{id}/attendee")
    fun updateSessionAttendee(
        httpServletRequest: HttpServletRequest,
        @PathVariable @Size(min = 5, max = 35, message = "Invalid identifier provided") id: String,
        @Valid @RequestBody request: UpdateSessionAttendee
    ): ResponseEntity<Void> {
        val context = super.extractContextFromRequest(httpServletRequest)
        val sessionAttendee = SessionAttendee.Mapper.from(context.auth0UserId, request.status, request.note)

        sessionBo.updateAttendee(id, sessionAttendee)

        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }

    @GetMapping("/{id}/attendee")
    fun getSessionAttendeesDetails(
        @PathVariable @Size(min = 5, max = 35, message = "Invalid identifier provided") id: String
    ): GetSessionAttendeeDetailsResponse {
        val attendeeDetails = sessionBo.getAttendeeDetails(id).map { attendee ->
            attendee.id to attendee
        }.toMap()

        return GetSessionAttendeeDetailsResponse(attendeeDetails)
    }
}