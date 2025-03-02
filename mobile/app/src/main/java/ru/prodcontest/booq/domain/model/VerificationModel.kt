package ru.prodcontest.booq.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

data class VerificationUserModel(
    val avatarUrl: String?,
    val email: String,
    val id: UUID,
    val name: String,
    val role: UserRole,
    val surname: String
)

data class VerificationModel(
    val document: String,
    val user: VerificationUserModel
)

