package ru.prodcontest.booq.domain.model

import java.time.LocalDateTime


data class QrVerificationModel(
    val valid: Boolean,
    val bookingData: QrVerificationData?
)

data class QrVerificationData(
    val buildingName: String,
    val itemName: String,
    val spaceName: String,
    val timeEnd: LocalDateTime,
    val timeStart: LocalDateTime,
    val userEmail: String
)