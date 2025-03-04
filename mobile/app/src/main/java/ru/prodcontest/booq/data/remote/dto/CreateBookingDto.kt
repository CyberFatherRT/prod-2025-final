package ru.prodcontest.booq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBookingDto(
    @SerialName("coworking_id")
    val coworkingId: String,
    @SerialName("coworking_item_id")
    val coworkingItemId: String,
    @SerialName("time_start")
    val timeStart: String,
    @SerialName("time_end")
    val timeEnd: String
)
