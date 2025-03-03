package ru.prodcontest.booq.domain.model

import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.Serializable

@Serializable
data class Point(
    val x: Int,
    val y: Int
) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
    operator fun minus(other: Point) = Point(x - other.x, y - other.y)
    operator fun times(other: Float) = Offset(x * other, y * other)

    fun toOffset(cellSize: Float) = Offset(x * cellSize.toFloat(), y * cellSize.toFloat())

    fun rebaseFromBottomLeft(height: Int) = Point(x, height - y - 1)
}
