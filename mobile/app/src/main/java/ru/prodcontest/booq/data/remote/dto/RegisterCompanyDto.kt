package ru.prodcontest.booq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterCompanyDto(
    @SerialName("domain")
    val domain: String,
    @SerialName("name")
    val name: String
)