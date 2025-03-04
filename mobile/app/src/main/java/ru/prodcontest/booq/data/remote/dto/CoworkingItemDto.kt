package ru.prodcontest.booq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.domain.model.Point

@Serializable
data class CoworkingItemDto(
    @SerialName("base_point")
    val basePoint: Point,
    val description: String?,
    val id: String,
    @SerialName("item_id")
    val itemId: String,
    val name: String
)
