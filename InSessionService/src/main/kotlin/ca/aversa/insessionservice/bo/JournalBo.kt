package ca.aversa.insessionservice.bo

import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.exception.JournalBoException
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.model.Journal
import ca.aversa.insessionservice.model.entity.UserJournalTableRow
import ca.aversa.insessionservice.util.Utils
import ca.aversa.insessionservice.util.Utils.createContainsFilterExpression
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ConditionalOperator

class JournalBo(private val journalDao: BasicDao<UserJournalTableRow>) {

    fun update(journal: Journal) {
        try {
            // Get existing journal (to update) or create a new one if journalId is null
            val tableRow: UserJournalTableRow = journal.id?.let { id ->
                val existingJournal = fetchFromDynamo(journal.userId, id)
                existingJournal.title = journal.title
                existingJournal.text = journal.text
                existingJournal.keywords = journal.keywords

                existingJournal
            } ?: journal.toEntity(journal.userId, Utils.createSecureId())

            journalDao.update(tableRow)
        }
        catch (e: Exception) {
            when(e) {
                is ResourceNotFoundException, is JournalBoException -> throw e
                else -> throw JournalBoException(cause = e)
            }
        }
    }

    fun get(userId: String, journalId: String): Journal {
        val row = fetchFromDynamo(userId, journalId)

        return Journal.Mapper.from(row)
    }

    fun getAll(userId: String, keywords: List<String>): List<Journal> {
        return queryFromDynamo(userId, keywords).map(Journal.Mapper::from)
    }

    fun updateKeywords(userId: String, journalId: String, keywords: List<String>) {
        try {
            val row = fetchFromDynamo(userId, journalId)
            row.keywords = keywords

            journalDao.update(row)
        }
        catch (e: Exception) {
            when(e) {
                is ResourceNotFoundException -> throw e
                else -> throw JournalBoException(cause = e)
            }
        }
    }

    private fun fetchFromDynamo(userId: String, journalId: String): UserJournalTableRow {
        try {
            return journalDao.get(userId, journalId)
                ?: throw ResourceNotFoundException("Journal with id $journalId not found for user $userId")
        }
        catch (e: Exception) {
            when(e) {
                is ResourceNotFoundException -> throw e
                else -> throw JournalBoException(cause = e)
            }
        }
    }

    private fun queryFromDynamo(userId: String, keywords: List<String>): List<UserJournalTableRow> {
        try {
            var filterExpression: String? = null
            val keyConditionExpression = "id = :id"
            val valuesMap = mutableMapOf(
                ":id" to AttributeValue().withS(userId),
            )

            if(keywords.isNotEmpty()) {
                val filterExpressionAndValueMap = createContainsFilterExpression("keywords", ":keyword",
                    keywords, ConditionalOperator.OR
                )

                filterExpression = filterExpressionAndValueMap.first
                valuesMap.putAll(filterExpressionAndValueMap.second)
            }

            return journalDao.query(keyConditionExpression, valuesMap, filterExpression)
        }
        catch (e: Exception) {
            throw JournalBoException(cause = e)
        }
    }
}