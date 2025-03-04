package ru.prodcontest.booq.data.remote.dto

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import ru.prodcontest.booq.domain.model.BookingCompanyModel
import ru.prodcontest.booq.domain.model.BookingIdData
import ru.prodcontest.booq.domain.model.BookingModel
import ru.prodcontest.booq.domain.model.BookingTextModel
import ru.prodcontest.booq.domain.model.BookingTime
import java.time.Instant
import java.time.LocalDateTime

@JsonIgnoreUnknownKeys
@Serializable
data class BookingDto(

    @SerialName("id")
    val id: String,
    @SerialName("coworking_item_id")
    val itemId: String,
    @SerialName("coworking_space_id")
    val spaceId: String,
    @SerialName("user_id")
    val userId: String,

    @SerialName("coworking_item_name")
    val name: String,
    @SerialName("coworking_item_description")
    val label: String? = "",

    @SerialName("building_address")
    val companyAddress: String,
    val itemName: String = name,
    @SerialName("coworking_space_name")
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
                itemId = itemId,
                spaceId = spaceId,
                userId = userId
            ),
            company = BookingCompanyModel(
                address = companyAddress
            ),
            name = BookingTextModel(
                id = itemName,
                label = label ?: "",
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