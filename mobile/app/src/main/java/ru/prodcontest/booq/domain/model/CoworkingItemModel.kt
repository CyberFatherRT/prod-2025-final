package ru.prodcontest.booq.domain.model

import androidx.compose.ui.geometry.Offset
import ru.prodcontest.booq.data.remote.dto.CompanyItemDto

data class CoworkingItemModel(
    val description: String?,
    val id: String,
    val item: CompanyItemDto,
    val name: String,
    val basePoint: Point,
    val occupied: List<BookingSpan>
) {
    fun massCenter(cellSize: Float) =
        Offset(
            (item.offsets.sumOf { it.x }.toFloat() / item.offsets.size + basePoint.x) * cellSize + cellSize / 2,
            (item.offsets.sumOf { it.y }.toFloat() / item.offsets.size + basePoint.y) * cellSize + cellSize / 2
        )
    val offsets: List<Point>
        get() = item.offsets

    fun isAvailable(span: BookingSpan) =
        occupied.none { it.start.isAfter(span.start) && it.end.isBefore(span.end) }
}