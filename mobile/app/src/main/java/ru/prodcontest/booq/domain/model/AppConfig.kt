package ru.prodcontest.booq.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val token: String?
)
