package ru.prodcontest.booq.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginDto(
    val email: String,
    val password: String,
    val domain: String
)
