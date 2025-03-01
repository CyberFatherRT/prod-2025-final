package ru.prodcontest.booq.domain.model

data class UserData(
    val username: String,
    val email: String,
    val avatarUrl: String? = null,
    val role: UserRole,
    val isVerification: Boolean, // Проверены ли данные о пользователе.
    val isAuthorized: Boolean, // Авторизован ли пользователь.
)

enum class UserRole {
    ADMIN,
    STUDENTS,
    GUEST,
    VerifiedGuest
}