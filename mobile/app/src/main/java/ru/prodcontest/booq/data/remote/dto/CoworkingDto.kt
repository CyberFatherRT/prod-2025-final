package ru.prodcontest.booq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoworkingDto(
    val address: String,
    @SerialName("building_id")
    val buildingId: String,
    @SerialName("company_id")
    val companyId: String,
    val height: Int,
    val width: Int,
    val id: String
)
