package ca.aversa.insessionservice.model

import ca.aversa.insessionservice.model.entity.UserJournalTableRow
import ca.aversa.insessionservice.model.request.UpdateJournalRequest
import java.time.ZonedDateTime

data class Journal(

    val userId: String,
    val title: String,
    val text: String,
    val updatedDateTime: ZonedDateTime = ZonedDateTime.now(),
    val keywords: List<String> = listOf(),
    val id: String? = null
) {
    fun toEntity(userId: String, id: String): UserJournalTableRow {
        return UserJournalTableRow(
            userId,
            id,
            title,
            text,
            updatedDateTime,
            keywords
        )
    }

    fun toSimplifiedJournal(): SimplifiedJournal {
        return SimplifiedJournal(
            id ?: "",
            title,
            updatedDateTime, keywords
        )
    }

    object Mapper {
        fun from(userId: String, request: UpdateJournalRequest, journalId: String? = null): Journal {
            return Journal(
                userId,
                request.title,
                request.text,
                id = journalId,
                keywords = request.keywords
            )
        }

        fun from(row: UserJournalTableRow): Journal {
            return Journal(
                row.userId,
                row.title,
                row.text,
                row.updatedDateTime,
                row.keywords,
                id = row.id
            )
        }
    }
}

data class SimplifiedJournal(

    val id: String,
    val title: String,
    val dateTime: ZonedDateTime,
    val keywords: List<String>,
)

