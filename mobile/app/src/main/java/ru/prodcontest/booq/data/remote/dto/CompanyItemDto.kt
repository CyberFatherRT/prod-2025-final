package ru.prodcontest.booq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.domain.model.Point

@Serializable
data class CompanyItemDto(
    val bookable: Boolean,
    @SerialName("company_id")
    val companyId: String,
    val description: String?,
    val icon: String?,
    val id: String,
    val name: String,
    val offsets: List<Point>,
    val color: String
) {
    fun withFixedOffsets() = this.copy(
        offsets = this.offsets.map { it.copy(y = -it.y) }
    )
}
