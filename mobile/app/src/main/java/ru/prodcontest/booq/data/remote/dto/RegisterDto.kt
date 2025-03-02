package ru.prodcontest.booq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterDto(
    @SerialName("name")
    val name: String,
    @SerialName("surname")
    val surname: String,
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String,
    @SerialName("company_domain")
    val companyDomain: String
)
