package ru.prodcontest.booq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias PlacesDto = List<PlaceDto>

@Serializable
data class PlaceDto(
    val address: String,
    @SerialName("company_id")
    val companyId: String,
    val id: String
)
