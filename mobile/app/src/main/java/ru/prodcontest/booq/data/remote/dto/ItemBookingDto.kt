package ru.prodcontest.booq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.domain.model.BookingSpan
import java.time.LocalDateTime

@Serializable
data class ItemBookingDto(
    @SerialName("company_id")
    val companyId: String,
    @SerialName("coworking_item_id")
    val coworkingItemId: String,
    @SerialName("coworking_space_id")
    val coworkingSpaceId: String,
    val id: String,
    @SerialName("time_end")
    val timeEnd: String,
    @SerialName("time_start")
    val timeStart: String,
    @SerialName("user_id")
    val userId: String
) {
    fun toBookingSpan() = BookingSpan(
        start = LocalDateTime.parse(this.timeStart),
        end = LocalDateTime.parse(this.timeEnd)
    )
}
