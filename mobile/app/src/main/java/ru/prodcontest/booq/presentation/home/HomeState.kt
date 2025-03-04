package ru.prodcontest.booq.presentation.home

import ru.prodcontest.booq.domain.model.BookingModel

data class HomeState(
    val bookings: List<BookingModel>,
    val isLoading: Boolean,
    val isProcessDelete: Boolean = true,
    val qrCode: QrCodeInfo,
    val error: String?,
    val isAdmin: Boolean,
    val isUnverified: Boolean
)

data class QrCodeInfo(
    val token: String = "",
    val state: QrCodeState
)

sealed class QrCodeState {
    data object Loading : QrCodeState()
    data class Error(val message: String) : QrCodeState()
    data object Ok : QrCodeState()
}