package ru.prodcontest.booq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.domain.model.UserModel
import ru.prodcontest.booq.domain.model.UserRole
import java.util.UUID

@Serializable
data class ProfileDto(
    @SerialName("name")
    val name: String,
    @SerialName("surname")
    val surname: String,
    @SerialName("avatar")
    val avatarUrl: String,
    @SerialName("role")
    val role: String,
    @SerialName("pending_verification")
    val pendingVerification: Boolean,
    @SerialName("company_id")
    val companyId: String
) {
    fun toModel() = UserModel(
        name = name,
        surname = surname,
        avatarUrl = avatarUrl,
        role = when(role) {
            "admin" -> UserRole.Admin
            "student" -> UserRole.Student
            "guest" -> UserRole.Guest
            "verified_guest" -> UserRole.VerifiedGuest
            else -> throw Exception("invalid role")
        },
        companyId = UUID.fromString(companyId)
    )
}
