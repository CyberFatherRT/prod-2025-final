package ru.prodcontest.booq.domain.model

import java.time.LocalDateTime

data class BookingSpan(
    val start: LocalDateTime,
    val end: LocalDateTime
)
