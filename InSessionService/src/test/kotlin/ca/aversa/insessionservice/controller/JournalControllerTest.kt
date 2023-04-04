package ca.aversa.insessionservice.controller

import ca.aversa.insessionservice.bo.JournalBo
import ca.aversa.insessionservice.context.RequestAttributeType
import ca.aversa.insessionservice.model.Journal
import ca.aversa.insessionservice.util.JournalTestUtils
import ca.aversa.insessionservice.util.TestDefaults
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import javax.servlet.http.HttpServletRequest

internal class JournalControllerTest {

    private val context = TestDefaults.TEST_CLINICIAN_CONTEXT
    private val httpServletRequest: HttpServletRequest = mockk()
    private val journalBo: JournalBo = mockk()
    private val journalController = JournalController(journalBo)
    private val journalId = "journalId"

    @BeforeEach
    fun setup() {
        every {
            httpServletRequest.getAttribute(RequestAttributeType.CONTEXT.toString())
        }.returns(context)
    }

    @Test
    fun testUpdateJournal_CallJournalBoToUpdateJournal_SuccessfulResponse() {
        val request = JournalTestUtils.createSampleUpdateJournalRequest()

        justRun {
            journalBo.update(any())
        }

        val response = journalController.updateJournal(httpServletRequest, null, request)

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
    }

    @Test
    fun testUpdateJournal_CallJournalBoToUpdateExistingJournal_SuccessfulResponse() {
        val request = JournalTestUtils.createSampleUpdateJournalRequest()

        justRun {
            journalBo.update(any())
        }

        val response = journalController.updateJournal(httpServletRequest, journalId, request)

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
    }

    @Test
    fun testGetJournal_FetchJournalFromJournalBo_ReturnJournal() {
        val expectedJournal = JournalTestUtils.createSampleJournal()

        every {
            journalBo.get(context.auth0UserId, journalId)
        }.returns(expectedJournal)

        val actualJournal = journalController.getJournal(httpServletRequest, journalId)

        assertEquals(expectedJournal, actualJournal)
    }

    @Test
    fun testUpdateKeywords_CallJournalBoToUpdateKeywords_SuccessfulResponse() {
        val request = JournalTestUtils.createSampleUpdateKeywordsRequest()

        justRun {
            journalBo.updateKeywords(context.auth0UserId, journalId, request.keywords)
        }

        val response = journalController.updateKeywords(httpServletRequest, journalId, request)

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
    }

    @Test
    fun testGetJournals_GetUserJournalsWithoutKeywords_ReturnSimplifiedVersionOfJournals() {
        val request = JournalTestUtils.createSampleGetJournalsForUsersRequest()
        val expectedJournals = listOf(
            JournalTestUtils.createSampleJournal()
        )

        every {
            journalBo.getAll(context.auth0UserId, emptyList())
        }.returns(expectedJournals)

        val response = journalController.getJournals(httpServletRequest, request)

        assertEquals(expectedJournals.map(Journal::toSimplifiedJournal), response.journals)
    }

    @Test
    fun testGetJournals_GetUserJournalsWithKeywords_ReturnSimplifiedVersionOfJournals() {
        val request = JournalTestUtils.createSampleGetJournalsForUsersRequest(
            keywords = TestDefaults.TEST_JOURNAL_KEYWORDS.joinToString(",")
        )
        val expectedJournals = listOf(
            JournalTestUtils.createSampleJournal(keywords = TestDefaults.TEST_JOURNAL_KEYWORDS)
        )

        every {
            journalBo.getAll(context.auth0UserId, TestDefaults.TEST_JOURNAL_KEYWORDS)
        }.returns(expectedJournals)

        val response = journalController.getJournals(httpServletRequest, request)

        assertEquals(expectedJournals.map(Journal::toSimplifiedJournal), response.journals)
    }
}