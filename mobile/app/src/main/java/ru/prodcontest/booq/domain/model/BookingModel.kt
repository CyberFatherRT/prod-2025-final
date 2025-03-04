package ru.prodcontest.booq.domain.model

import java.time.LocalDateTime

data class BookingModel(
    val idData: BookingIdData,
    val company: BookingCompanyModel,
    val name: BookingTextModel,
    val time: BookingTime
)

data class BookingCompanyModel(
    val address: String
)

data class BookingIdData(
    val id: String,
    val itemId: String,
    val spaceId: String,
    val userId: String? = null
)

data class BookingTextModel(
    val id: String,
    val label: String,
    val item: String,
    val space: String
)

data class BookingTime(
    val start: LocalDateTime,
    val end: LocalDateTime
)
