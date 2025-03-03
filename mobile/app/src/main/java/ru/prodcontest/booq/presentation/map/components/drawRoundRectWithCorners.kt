package ru.prodcontest.booq.presentation.map.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

enum class Corner {
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight
}

fun DrawScope.drawRoundRectWithCorners(
    color: Color,
    topLeft: Offset,
    size: Size,
    radius: Float,
    corners: List<Corner>
) {
    drawRoundRect(color, topLeft, size, CornerRadius(radius, radius))
    corners.forEach { corner ->
        when (corner) {
            Corner.TopLeft -> {
                drawRect(color, topLeft, size / 2F)
            }

            Corner.TopRight -> {
                drawRect(color, Offset(topLeft.x + size.width / 2, topLeft.y), size / 2F)
            }

            Corner.BottomLeft -> {
                drawRect(color, Offset(topLeft.x, topLeft.y + size.height / 2), size / 2F)
            }

            Corner.BottomRight -> {
                drawRect(
                    color,
                    Offset(topLeft.x + size.width / 2, topLeft.y + size.height / 2),
                    size / 2F
                )
            }
        }
    }
}