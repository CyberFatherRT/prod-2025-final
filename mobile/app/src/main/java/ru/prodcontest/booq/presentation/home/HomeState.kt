package ru.prodcontest.booq.presentation.home

import ru.prodcontest.booq.domain.model.BookingModel

data class HomeState(
    val bookings: List<BookingModel>,
    val isLoading: Boolean,
    val error: String?
)