package ru.prodcontest.booq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.Constants

@Serializable
data class RegisterDto(
    val username: String,
    val email: String,
    val password: String,
    @SerialName("company_id")
    val companyId: String = Constants.COMPANY_ID
)
