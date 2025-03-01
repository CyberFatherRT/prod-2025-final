package ru.prodcontest.booq.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable;

@Serializable
data class AppConfig(
    @SerialName("is_admin")
    val isAdmin: Boolean = false,
    @SerialName("is_authorized")
    val isAuthorized: Boolean = false
)
