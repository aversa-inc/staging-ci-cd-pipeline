package ca.aversa.insessionservice.bo

import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.exception.JournalBoException
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.model.Journal
import ca.aversa.insessionservice.model.entity.UserJournalTableRow
import ca.aversa.insessionservice.util.JournalTestUtils.createSampleJournal
import ca.aversa.insessionservice.util.TestDefaults
import ca.aversa.insessionservice.util.Utils
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class JournalBoTest {

    private val journalDao: BasicDao<UserJournalTableRow> = mockk()
    private val journalBo = JournalBo(journalDao)
    private val userId = TestDefaults.TEST_CLINICIAN_AUTH0_USER_ID
    private val journalId = "journalId"
    private val keywords = TestDefaults.TEST_JOURNAL_KEYWORDS
    private val keyExpression = "id = :id"

    @BeforeEach
    fun setup() {
        mockkObject(Utils)

        every {
            Utils.createSecureId()
        }.returns(journalId)
    }

    @Test
    fun testUpdate_CreateNewJournalIfIdNotPresent_Successful() {
        val expectedJournal = createSampleJournal()

        justRun {
            journalDao.update(expectedJournal.toEntity(userId, journalId))
        }

        journalBo.update(expectedJournal)

        verify(exactly = 0) {
            journalDao.get(userId, journalId)
        }
    }

    @Test
    fun testUpdate_UpdateExistingJournal_Successful() {
        val existingJournal = createSampleJournal(journalId = journalId)
        val existingJournalTableRow = existingJournal.toEntity(userId, journalId)
        val journalToUpdate = createSampleJournal(userId, "updatedtext", "updatedText",
            existingJournal.updatedDateTime, journalId = journalId
        )

        every {
            journalDao.get(userId, journalId)
        }.returns(existingJournalTableRow)

        justRun {
            journalDao.update(journalToUpdate.toEntity(existingJournalTableRow.userId, existingJournalTableRow.id))
        }

        journalBo.update(journalToUpdate)
    }

    @Test
    fun testUpdate_ExistingJournalNotFound_ThrowResourceNotFoundException() {
        val journalToUpdate = createSampleJournal(journalId = journalId)

        every {
            journalDao.get(userId, journalId)
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            journalBo.update(journalToUpdate)
        }
    }

    @Test
    fun testUpdate_FetchingExistingJournalThrowsException_ThrowBoException() {
        val journalToUpdate = createSampleJournal(journalId = journalId)

        every {
            journalDao.get(userId, journalId)
        }.throws(RuntimeException("Something broke"))

        assertThrows<JournalBoException> {
            journalBo.update(journalToUpdate)
        }
    }

    @Test
    fun testUpdate_UpdatingThrowsException_ThrowBoException() {
        val expectedJournal = createSampleJournal()

        every {
            journalDao.update(expectedJournal.toEntity(userId, journalId))
        }.throws(RuntimeException("somethign broke"))

        assertThrows<JournalBoException> {
            journalBo.update(expectedJournal)
        }
    }

    @Test
    fun testGet_JournalWithGivenIdExists_ReturnJournal() {
        val expectedJournal = createSampleJournal(keywords = emptyList(), journalId = journalId)

        every {
            journalDao.get(userId, journalId)
        }.returns(expectedJournal.toEntity(userId, journalId))

        val actualJournal = journalBo.get(userId, journalId)

        assertEquals(expectedJournal, actualJournal)
    }

    @Test
    fun testGet_ExistingJournalNotFound_ThrowResourceNotFoundException() {
        every {
            journalDao.get(userId, journalId)
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            journalBo.get(userId, journalId)
        }
    }

    @Test
    fun testGet_FetchingExistingJournalThrowsException_ThrowBoException() {
        every {
            journalDao.get(userId, journalId)
        }.throws(RuntimeException("Something broke"))

        assertThrows<JournalBoException> {
            journalBo.get(userId, journalId)
        }
    }

    @Test
    fun testUpdateKeywords_ExistingJournalFoundAndKeywordsUpdated_Successful() {
        val existingJournal = createSampleJournal(journalId = journalId)
        val existingJournalTableRow = existingJournal.toEntity(userId, journalId)

        every {
            journalDao.get(userId, journalId)
        }.returns(existingJournalTableRow)

        existingJournalTableRow.keywords = keywords

        justRun {
            journalDao.update(existingJournalTableRow)
        }

        journalBo.updateKeywords(userId, journalId, keywords)
    }

    @Test
    fun testUpdateKeywords_ExistingJournalNotFound_ThrowResourceNotFoundException() {
        every {
            journalDao.get(userId, journalId)
        }.returns(null)

        assertThrows<ResourceNotFoundException> {
            journalBo.updateKeywords(userId, journalId, keywords)
        }
    }

    @Test
    fun testUpdateKeywords_DdbThrowsExceptionWhileFetchingExistingJournal_ThrowBoException() {
        every {
            journalDao.get(userId, journalId)
        }.throws(RuntimeException("Something broke"))

        assertThrows<JournalBoException> {
            journalBo.updateKeywords(userId, journalId, keywords)
        }
    }

    @Test
    fun testUpdateKeywords_ExceptionThrownWhileUpdating_ThrowBoException() {
        val existingJournal = createSampleJournal(journalId = journalId)
        val existingJournalTableRow = existingJournal.toEntity(userId, journalId)

        every {
            journalDao.get(userId, journalId)
        }.returns(existingJournalTableRow)

        existingJournalTableRow.keywords = keywords

        every {
            journalDao.update(existingJournalTableRow)
        }.throws(RuntimeException("Something broke"))

        assertThrows<JournalBoException> {
            journalBo.updateKeywords(userId, journalId, keywords)
        }
    }

    @Test
    fun testGetWithKeywords_QueryDynamoDbWithoutKeywords_ReturnResults() {
        val existingJournals = listOf(
            createSampleJournal().toEntity(userId, journalId),
            createSampleJournal().toEntity(userId, "someotherjournalid")
        )

        every {
            journalDao.query(keyExpression, createAttributeValueMap(false))
        }.returns(existingJournals)

        val actualJournals = journalBo.getAll(userId, emptyList())

        assertEquals(existingJournals.map(Journal.Mapper::from), actualJournals)
    }

    @Test
    fun testGetWithKeywords_QueryDynamoDbWithKeywords_ReturnResults() {
        val existingJournals = listOf(
            createSampleJournal(keywords = listOf(keywords[0])).toEntity(userId, journalId),
            createSampleJournal(keywords = listOf(keywords[1])).toEntity(userId, "someotherjournalid")
        )

        every {
            journalDao.query(keyExpression, createAttributeValueMap(true), any())
        }.returns(existingJournals)

        val actualJournals = journalBo.getAll(userId, keywords)

        assertEquals(existingJournals.map(Journal.Mapper::from), actualJournals)
    }

    @Test
    fun testGetWithKeywords_DynamoDbThrowsException_ThrowBoException() {
        every {
            journalDao.query(any(), any())
            journalDao.query(any(), any(), any())
        }.throws(RuntimeException("Something broke"))

        assertThrows<JournalBoException> {
            journalBo.getAll(userId, emptyList())
            journalBo.getAll(userId, keywords)
        }
    }

    private fun createAttributeValueMap(addKeywords: Boolean): Map<String, AttributeValue> {
        val attributeValueMap = mutableMapOf(
            ":id" to AttributeValue().withS(userId)
        )

        if(addKeywords) {
            attributeValueMap[":keyword0"] = AttributeValue().withS(keywords[0])
            attributeValueMap[":keyword1"] = AttributeValue().withS(keywords[1])
            attributeValueMap[":keyword2"] = AttributeValue().withS(keywords[2])
        }

        return attributeValueMap
    }
}