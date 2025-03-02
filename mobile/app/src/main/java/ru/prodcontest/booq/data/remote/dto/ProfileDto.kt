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
    val avatarUrl: String? = null,
    @SerialName("role")
    val role: String,
    @SerialName("pending_verification")
    val pendingVerification: Boolean,
    @SerialName("company_id")
    val companyId: String,
    @SerialName("email")
    val email: String
) {
    fun toModel() = UserModel(
        name = name,
        surname = surname,
        avatarUrl = avatarUrl,
        role = when(role) {
            "ADMIN" -> UserRole.Admin
            "STUDENT" -> UserRole.Student
            "GUEST" -> UserRole.Guest
            "VERIFIEDGUEST" -> UserRole.VerifiedGuest
            else -> throw Exception("invalid role")
        },
        companyId = UUID.fromString(companyId),
        email = email,
        pendingVerification = pendingVerification
    )
}
