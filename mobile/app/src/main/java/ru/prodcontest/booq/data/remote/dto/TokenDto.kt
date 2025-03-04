package ru.prodcontest.booq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenDto(@SerialName("jwt") val token: String)

@Serializable
data class QrTokenDto(@SerialName("token") val token: String)