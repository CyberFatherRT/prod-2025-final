package ru.prodcontest.booq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBookingResponseDto(
    @SerialName("company_id")
    var companyId: String,
    @SerialName("coworking_item_id")
    var coworkingItemId: String,
    @SerialName("coworking_space_id")
    var coworkingSpaceId: String,
    @SerialName("id")
    var id: String,
    @SerialName("time_end")

    var timeEnd: String,
    @SerialName("time_start") var timeStart: String,
    @SerialName("user_id") var userId: String
)
