package ru.prodcontest.booq.data.remote.dto

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.domain.model.BookingCompanyModel
import ru.prodcontest.booq.domain.model.BookingIdData
import ru.prodcontest.booq.domain.model.BookingModel
import ru.prodcontest.booq.domain.model.BookingTextModel
import ru.prodcontest.booq.domain.model.BookingTime
import java.time.Instant
import java.time.LocalDateTime

@Serializable
data class BookingDto(

    @SerialName("id")
    val id: String,
    @SerialName("company_id")
    val companyId: String,
    @SerialName("coworking_item_id")
    val itemId: String,
    @SerialName("coworking_space_id")
    val spaceId: String,
    @SerialName("user_id")
    val userId: String,

    @SerialName("booking_item_name")
    val name: String,
    @SerialName("booking_item_description")
    val label: String? = "",

    @SerialName("company_name")
    val companyName: String,
    @SerialName("building_address")
    val companyAddress: String,
    @SerialName("coworking_item_name")
    val itemName: String? = "",
    @SerialName("coworking_item_space")
    val spaceName: String? = "",

    @SerialName("time_start")
    val timeStart: String,
    @SerialName("time_end")
    val timeEnd: String,

) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun toModel(): BookingModel {
        return BookingModel(
            idData = BookingIdData(
                id = id,
                companyId = companyId,
                itemId = itemId,
                spaceId = spaceId,
                userId = userId
            ),
            company = BookingCompanyModel(
                id = companyId,
                name = companyName,
                address = companyAddress
            ),
            name = BookingTextModel(
                id = itemId,
                label = label ?: "",
                company = companyName,
                item = itemName ?: "",
                space = spaceName ?: ""
            ),
            time = BookingTime(
                start = LocalDateTime.parse(timeStart),
                end = LocalDateTime.parse(timeEnd)
            )
        )
    }
}