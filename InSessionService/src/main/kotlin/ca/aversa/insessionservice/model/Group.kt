package ca.aversa.insessionservice.model

import ca.aversa.insessionservice.model.entity.UserGroupTableRow
import ca.aversa.insessionservice.model.request.CreateGroupRequest

data class Group(

    val clinician: String,
    val clients: List<String>,
    val name: String,
    val id: String? = null
) {
    fun toEntity(groupId: String): UserGroupTableRow {
        return UserGroupTableRow(
            clinician,
            groupId,
            clients as MutableList<String>,
            name
        )
    }

    object Mapper {
        fun from(clinicianId: String, request: CreateGroupRequest): Group {
            return Group(
                clinicianId,
                request.clientIds,
                request.name
            )
        }

        fun from(row: UserGroupTableRow): Group {
            return Group(
                row.clinicianId,
                row.clients,
                row.name,
                row.groupId
            )
        }
    }
}
