package ca.aversa.insessionservice.util

import ca.aversa.insessionservice.model.Journal
import ca.aversa.insessionservice.model.request.GetJournalsForUserRequest
import ca.aversa.insessionservice.model.request.UpdateJournalKeywords
import ca.aversa.insessionservice.model.request.UpdateJournalRequest
import java.time.ZonedDateTime

object JournalTestUtils {

    fun createSampleJournal(
        userId: String = TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID,
        title: String = TestDefaults.TEST_JOURNAL_TITLE,
        text: String = TestDefaults.TEST_JOURNAL_TEXT,
        updatedDateTime: ZonedDateTime = TestDefaults.TEST_JOURNAL_DATE,
        keywords: List<String> = TestDefaults.TEST_JOURNAL_KEYWORDS,
        journalId: String? = null
    ): Journal {
        return Journal(userId, title, text, updatedDateTime, keywords, journalId)
    }

    fun createSampleUpdateJournalRequest(
        title: String = TestDefaults.TEST_JOURNAL_TITLE,
        text: String = TestDefaults.TEST_JOURNAL_TEXT,
        keywords: List<String> = TestDefaults.TEST_JOURNAL_KEYWORDS
    ): UpdateJournalRequest {
        return UpdateJournalRequest(title, text, keywords)
    }

    fun createSampleUpdateKeywordsRequest(
        keywords: List<String> = TestDefaults.TEST_JOURNAL_KEYWORDS
    ): UpdateJournalKeywords {
        return UpdateJournalKeywords(
            keywords
        )
    }

    fun createSampleGetJournalsForUsersRequest(keywords: String? = null): GetJournalsForUserRequest {
        return GetJournalsForUserRequest(keywords)
    }
}