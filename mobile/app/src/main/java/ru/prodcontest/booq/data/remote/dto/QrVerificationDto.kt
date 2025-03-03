package ru.prodcontest.booq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.domain.model.QrVerificationData
import ru.prodcontest.booq.domain.model.QrVerificationModel
import java.time.LocalDateTime

@Serializable
data class QrVerificationDto(
    val valid: Boolean,
    @SerialName("booking_data")
    val bookingData: QrVerificationBooking?
) {
    fun toModel() = QrVerificationModel(
        valid = valid,
        bookingData = bookingData?.let {
            QrVerificationData(
                buildingName = it.buildingName,
                itemName = it.itemName,
                spaceName = it.spaceName,
                timeEnd = LocalDateTime.parse(it.timeEnd),
                timeStart = LocalDateTime.parse(it.timeStart),
                userEmail = it.userEmail
            )
        }
    )
}

@Serializable
data class QrVerificationBooking(
    @SerialName("building_name")
    val buildingName: String,
    @SerialName("item_name")
    val itemName: String,
    @SerialName("space_name")
    val spaceName: String,
    @SerialName("time_end")
    val timeEnd: String,
    @SerialName("time_start")
    val timeStart: String,
    @SerialName("user_email")
    val userEmail: String
)