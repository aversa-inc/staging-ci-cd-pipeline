package ca.aversa.insessionservice.model

import ca.aversa.insessionservice.model.entity.UserFamilyMemberDocument
import ca.aversa.insessionservice.model.request.UserFamilyMemberRequest

data class UserFamilyMember(

    val email: String,
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val emergencyContact: Boolean
) {

    fun toEntity(): UserFamilyMemberDocument {
        return UserFamilyMemberDocument(email, phoneNumber, firstName, lastName, emergencyContact)
    }

    object Mapper {
        fun from(request: UserFamilyMemberRequest): UserFamilyMember {
            return UserFamilyMember(
                request.email,
                request.phoneNumber,
                request.firstName,
                request.lastName,
                request.emergencyContact
            )
        }

        fun from(document: UserFamilyMemberDocument): UserFamilyMember {
            return UserFamilyMember(
                document.email,
                document.phoneNumber,
                document.firstName,
                document.lastName,
                document.emergencyContact
            )
        }
    }
}
