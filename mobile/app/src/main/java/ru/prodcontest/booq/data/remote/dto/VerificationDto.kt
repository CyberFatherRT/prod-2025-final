package ru.prodcontest.booq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.domain.model.UserRole
import ru.prodcontest.booq.domain.model.VerificationModel
import ru.prodcontest.booq.domain.model.VerificationUserModel
import java.util.UUID

@Serializable
data class VerificationUserDto(
    @SerialName("avatar")
    val avatarUrl: String? = null,
    @SerialName("email")
    val email: String,
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("surname")
    val surname: String,
    @SerialName("role")
    val role: String
) {
    fun toModel() = VerificationUserModel(
        id = UUID.fromString(id),
        name = name,
        surname = surname,
        avatarUrl = avatarUrl,
        role = when (role) {
            "ADMIN" -> UserRole.Admin
            "STUDENT" -> UserRole.Student
            "GUEST" -> UserRole.Guest
            "VERIFIEDGUEST" -> UserRole.VerifiedGuest
            else -> throw Exception("invalid role")
        },
        email = email
    )
}

@Serializable
data class  VerificationDto(
    @SerialName("document")
    val document: String,
    @SerialName("user")
    val user: VerificationUserDto
) {
    fun toModel() = VerificationModel(
        document = document,
        user = VerificationUserModel(
            id = UUID.fromString(user.id),
            name = user.name,
            surname = user.surname,
            avatarUrl = user.avatarUrl,
            role = when (user.role) {
                "ADMIN" -> UserRole.Admin
                "STUDENT" -> UserRole.Student
                "GUEST" -> UserRole.Guest
                "VERIFIEDGUEST" -> UserRole.VerifiedGuest
                else -> throw Exception("invalid role")
            },
            email = user.email
        )
    )
}

