package ca.aversa.insessionservice.controller

import ca.aversa.insessionservice.bo.JournalBo
import ca.aversa.insessionservice.model.Journal
import ca.aversa.insessionservice.model.request.GetJournalsForUserRequest
import ca.aversa.insessionservice.model.request.UpdateJournalKeywords
import ca.aversa.insessionservice.model.request.UpdateJournalRequest
import ca.aversa.insessionservice.model.response.GetJournalsForUserResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.constraints.Size

@RestController
@RequestMapping("/journal")
class JournalController(

    private val journalBo: JournalBo
): BaseController() {

    @GetMapping
    fun getJournals(httpServletRequest: HttpServletRequest,
                    @Valid request: GetJournalsForUserRequest): GetJournalsForUserResponse {
        val context = super.extractContextFromRequest(httpServletRequest)
        val keywords = request.keywords?.split(",")?.toList()
            ?: emptyList()

        val journals = journalBo.getAll(context.auth0UserId, keywords).map(Journal::toSimplifiedJournal)

        return GetJournalsForUserResponse(journals)
    }

    @PutMapping("/{id}", "")
    fun updateJournal(httpServletRequest: HttpServletRequest,
                      @PathVariable id: String?,
                      @Valid @RequestBody request: UpdateJournalRequest): ResponseEntity<Void> {
        val context = super.extractContextFromRequest(httpServletRequest)
        val journal = Journal.Mapper.from(context.auth0UserId, request, id)

        journalBo.update(journal)

        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }

    @GetMapping("/{id}")
    fun getJournal(httpServletRequest: HttpServletRequest,
                   @PathVariable @Size(min = 5, max = 35, message = "Invalid identifier provided") id: String): Journal {
        val context = super.extractContextFromRequest(httpServletRequest)

        return journalBo.get(context.auth0UserId, id)
    }

    @PatchMapping("/keywords/{id}")
    fun updateKeywords(httpServletRequest: HttpServletRequest,
                       @PathVariable @Size(min = 5, max = 35, message = "Invalid identifier provided") id: String,
                       @Valid @RequestBody request: UpdateJournalKeywords): ResponseEntity<Void> {
        val context = super.extractContextFromRequest(httpServletRequest)

        journalBo.updateKeywords(context.auth0UserId, id, request.keywords)

        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }
}