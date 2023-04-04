package ca.aversa.insessionservice.model

import ca.aversa.insessionservice.model.entity.SessionAttendeeDocument

data class SessionAttendee(

    val userId: String,
    val status: SessionAttendeeStatus,
    val note: String?
) {
    fun toEntity(): SessionAttendeeDocument {
        return SessionAttendeeDocument(userId, status, note)
    }

    object Mapper {
        fun from(
            userId: String,
            status: SessionAttendeeStatus = SessionAttendeeStatus.TENTATIVE,
            note: String? = null
        ): SessionAttendee {
            return SessionAttendee(userId, status, note)
        }

        fun from(document: SessionAttendeeDocument): SessionAttendee {
            return SessionAttendee(document.userId, document.status, document.note)
        }
    }
}

enum class SessionAttendeeStatus {
    TENTATIVE,
    ACCEPTED,
    DECLINED,
    CANCELLED
}

