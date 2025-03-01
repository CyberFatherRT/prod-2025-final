package ru.prodcontest.booq.domain.model

import java.util.UUID

data class UserModel(
    val name: String,
    val surname: String,
    val avatarUrl: String,
    val role: UserRole,
    val companyId: UUID
)

enum class UserRole {
    Admin,
    Student,
    Guest,
    VerifiedGuest
}
