package ru.prodcontest.booq.domain.model

import java.util.UUID

data class UserModel(
    val name: String,
    val email: String,
    val surname: String,
    val avatarUrl: String?,
    val role: UserRole,
    val companyId: UUID,
    val pendingVerification: Boolean
)

enum class UserRole {
    Admin,
    Student,
    Guest,
    VerifiedGuest
}
