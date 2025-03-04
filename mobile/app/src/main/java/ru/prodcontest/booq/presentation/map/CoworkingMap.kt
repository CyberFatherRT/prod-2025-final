package ru.prodcontest.booq.presentation.map

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.launch
import ru.prodcontest.booq.domain.model.BookingSpan
import ru.prodcontest.booq.domain.model.CoworkingItemModel
import ru.prodcontest.booq.domain.model.Point
import ru.prodcontest.booq.presentation.map.components.Corner
import ru.prodcontest.booq.presentation.map.components.drawRoundRectWithCorners
import ru.prodcontest.booq.presentation.util.AnimatableFloatSaver
import ru.prodcontest.booq.presentation.util.AnimatableOffsetSaver
import kotlin.math.roundToInt


const val CELL = 60f

fun minuteToTime(value: Float): String {
    var m = (value.rem(60) / 15).roundToInt() * 15
    var h = (value / 60).roundToInt()
    if (m == 60) {
        m = 0
        h += 1
    }
    if (h == 24) {
        m = 0
        h = 0
    }
    return "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoworkingMap(
    width: Int,
    height: Int,
    items: List<CoworkingItemModel>,
    onClickItem: (CoworkingItemModel) -> Unit,
    selectedSpan: BookingSpan
) {
    val camera = rememberSaveable(saver = AnimatableOffsetSaver) {
        Animatable(
            Offset.Zero,
            Offset.VectorConverter
        )
    }
    val cameraScaling = rememberSaveable(saver = AnimatableFloatSaver) { Animatable(1f) }
    val cameraAnimationScope = rememberCoroutineScope()
    val cameraScalingAnimationScope = rememberCoroutineScope()
    var invalidations by remember { mutableIntStateOf(0) }


    fun zoomTo(
        centroid: Offset,
        pan: Offset,
        newScale: Float,
        offsetAnimation: AnimationSpec<Offset> = snap(0),
        scalingAnimation: AnimationSpec<Float> = snap(0)
    ) {
        val oldScale = cameraScaling.value
        val limitedScale = if (newScale > 2) 2f else {
            if (newScale < 0.35) 0.35f else newScale
        }
        cameraAnimationScope.launch {
            camera.animateTo(
                (camera.value + centroid / oldScale) - (centroid / limitedScale + pan / oldScale),
                animationSpec = offsetAnimation
            )
        }
        cameraScalingAnimationScope.launch {
            cameraScaling.animateTo(limitedScale, animationSpec = scalingAnimation)
        }
    }

    fun focusToItem(item: CoworkingItemModel, canvasSize: IntSize) {
        cameraAnimationScope.launch {
            camera.animateTo(
                Offset(
                    item.massCenter(CELL).x - canvasSize.width / 4,
                    item.massCenter(CELL).y - canvasSize.height / 4 + canvasSize.height / 8
                ), animationSpec = tween(1000)
            )
        }
        cameraScalingAnimationScope.launch {
            cameraScaling.animateTo(2f, animationSpec = tween(1000))
        }
    }

    var canvasSize by remember { mutableStateOf(Size.Unspecified) }



    Canvas(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    val point = (it - Offset(
                        -camera.value.x * cameraScaling.value,
                        -camera.value.y * cameraScaling.value
                    )) / cameraScaling.value
                    Log.d("MEOW", "clicked $point ${cameraScaling.value}")
                    items.forEach { item ->
                        item.offsets.forEach { dot ->
                            val a = (dot + item.basePoint).toOffset(CELL)
                            if (point.x >= a.x && point.y >= a.y && point.x <= a.x + CELL && point.y <= a.y + CELL) {
                                if (item.item.bookable) {
                                    focusToItem(item, size)
                                    onClickItem(item)
                                }
                            }
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    zoomTo(centroid, pan, cameraScaling.value * zoom)
                }
            }
            .graphicsLayer {
                translationX = -camera.value.x * cameraScaling.value
                translationY = -camera.value.y * cameraScaling.value
                scaleX = cameraScaling.value
                scaleY = cameraScaling.value
                transformOrigin = TransformOrigin(0f, 0f)
            }
    ) {
        canvasSize = size
        invalidations.let { _ ->
            drawLine(Color.Gray, Offset(0f, 0f), Offset(width * CELL, 0f))
            drawLine(Color.Gray, Offset(0f, 0f), Offset(0f, height * CELL))
            drawLine(
                Color.Gray,
                Offset(width * CELL, 0f),
                Offset(width * CELL, height * CELL)
            )
            drawLine(
                Color.Gray,
                Offset(0f, height * CELL),
                Offset(width * CELL, height * CELL)
            )
            (0..width).forEach { xCell ->
                if (xCell != width) {
                    (1..4).forEach { offset ->
                        drawLine(
                            Color.Gray,
                            Offset(xCell * CELL + offset * (CELL / 4), 0f),
                            Offset(xCell * CELL + offset * (CELL / 4), height * CELL),
                            alpha = 0.5f,
                            strokeWidth = 0.5f
                        )
                    }
                }
                drawLine(
                    Color.Gray,
                    Offset(xCell * CELL, 0f),
                    Offset(xCell * CELL, height * CELL),
                    strokeWidth = 2f,
                    alpha = 0.9f
                )
            }
            (0..height).forEach { yCell ->
                if (yCell != height) {
                    (1..4).forEach { offset ->
                        drawLine(
                            Color.Gray,
                            Offset(0f, yCell * CELL + offset * (CELL / 4)),
                            Offset(width * CELL, yCell * CELL + offset * (CELL / 4)),
                            alpha = 0.5f,
                            strokeWidth = 0.5f
                        )
                    }
                }
                drawLine(
                    Color.Gray,
                    Offset(0f, yCell * CELL),
                    Offset(width * CELL, yCell * CELL),
                    strokeWidth = 2f,
                    alpha = 0.9f
                )
            }

            items.forEach { item ->
                var c = item.item.color
                if(!c.startsWith("#")) {
                   c = "#$c"
                }
                val color = Color(c.toColorInt())
                val color1 = if (item.item.bookable) {
                    color
                } else {
                    Color(color.red / 5f, color.green / 5f, color.blue / 5f)
                }
                val color2 = if(item.isAvailable(selectedSpan)) {
                    color1
                } else {
                    Color(color1.red / 5f, color1.green / 5f, color1.blue / 5f)
                }
                item.offsets.forEach { dot ->
                    val actualDot = item.basePoint + dot
                    val corners = mutableListOf<Corner>().apply {
                        if (item.offsets.any { it.x == dot.x - 1 && it.y == dot.y }) {
                            addAll(listOf(Corner.TopLeft, Corner.BottomLeft))
                        }
                        if (item.offsets.any { it.x == dot.x + 1 && it.y == dot.y }) {
                            addAll(listOf(Corner.TopRight, Corner.BottomRight))
                        }
                        if (item.offsets.any { it.y == dot.y - 1 && it.x == dot.x }) {
                            addAll(listOf(Corner.TopLeft, Corner.TopRight))
                        }
                        if (item.offsets.any { it.y == dot.y + 1 && it.x == dot.x }) {
                            addAll(listOf(Corner.BottomLeft, Corner.BottomRight))
                        }
                    }
                    drawRoundRectWithCorners(
                        color2,
                        Offset(actualDot.x * CELL - 2f, actualDot.y * CELL - 2f),
                        Size(CELL + 2f, CELL + 2f),
                        CELL / 4,
                        corners
                    )
                }

                val topLeftBoundX = item.offsets.minBy { it.x }.x
                val topLeftBoundY = item.offsets.minBy { it.y }.y
                val botRightBoundX = item.offsets.maxBy { it.x }.x
                val botRightBoundY = item.offsets.maxBy { it.y }.y
                (topLeftBoundX..botRightBoundX).forEach { xCell ->
                    (topLeftBoundY..botRightBoundY).forEach { yCell ->
                        val hasRight = item.offsets.any { it.x == xCell + 1 && it.y == yCell }
                        val hasLeft = item.offsets.any { it.x == xCell - 1 && it.y == yCell }
                        val hasTop = item.offsets.any { it.x == xCell && it.y == yCell - 1 }
                        val hasBottom = item.offsets.any { it.x == xCell && it.y == yCell + 1 }

                        val hasTopRight = hasTop && hasRight
                        val hasTopLeft = hasTop && hasLeft
                        val hasBottomRight = hasBottom && hasRight
                        val hasBottomLeft = hasBottom && hasLeft

                        if (hasTopRight || hasTopLeft || hasBottomRight || hasBottomLeft) {
                            val actualDot = item.basePoint + Point(xCell, yCell)
                            val rectOffset = actualDot * CELL
                            val rectSize = Size(CELL + 2f, CELL + 2f)

                            val cornersToRound = mutableListOf<Corner>()

                            if (hasTopRight) cornersToRound.add(Corner.TopRight)
                            if (hasTopLeft) cornersToRound.add(Corner.TopLeft)
                            if (hasBottomRight) cornersToRound.add(Corner.BottomRight)
                            if (hasBottomLeft) cornersToRound.add(Corner.BottomLeft)

                            drawRectWithCustomCorners(
                                color2,
                                offset = rectOffset,
                                size = rectSize,
                                cornerRadius = CELL / 4,
                                cornersToRound = cornersToRound,
                                cornersToKeepSquare = emptyList()
                            )
                        }
                    }
                }
            }
        }
    }

}

fun DrawScope.drawRectWithCustomCorners(
    color: Color,
    offset: Offset,
    size: Size,
    cornerRadius: Float,
    cornersToRound: List<Corner> = listOf(
        Corner.TopLeft,
        Corner.TopRight,
        Corner.BottomLeft,
        Corner.BottomRight
    ),
    cornersToKeepSquare: List<Corner> = emptyList()
) {
    val effectiveCornersToRound = cornersToRound.filter { it !in cornersToKeepSquare }

    drawIntoCanvas { canvas ->
        val checkpointCount = canvas.nativeCanvas.save()

        try {
            canvas.nativeCanvas.saveLayer(null, null)

            val paint = androidx.compose.ui.graphics.Paint()
            paint.color = color
            canvas.drawRect(
                offset.x,
                offset.y,
                offset.x + size.width,
                offset.y + size.height,
                paint
            )

            paint.blendMode = BlendMode.DstOut
            paint.color = Color.Black

            val path = Path().apply {
                moveTo(offset.x + cornerRadius, offset.y)

                lineTo(offset.x + size.width - cornerRadius, offset.y)

                if (Corner.TopRight in effectiveCornersToRound) {
                    arcTo(
                        rect = Rect(
                            left = offset.x + size.width - 2 * cornerRadius,
                            top = offset.y,
                            right = offset.x + size.width,
                            bottom = offset.y + 2 * cornerRadius
                        ),
                        startAngleDegrees = 270f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                } else {
                    lineTo(offset.x + size.width, offset.y)
                    lineTo(offset.x + size.width, offset.y + cornerRadius)
                }

                lineTo(offset.x + size.width, offset.y + size.height - cornerRadius)

                if (Corner.BottomRight in effectiveCornersToRound) {
                    arcTo(
                        rect = Rect(
                            left = offset.x + size.width - 2 * cornerRadius,
                            top = offset.y + size.height - 2 * cornerRadius,
                            right = offset.x + size.width,
                            bottom = offset.y + size.height
                        ),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                } else {
                    lineTo(offset.x + size.width, offset.y + size.height)
                    lineTo(offset.x + size.width - cornerRadius, offset.y + size.height)
                }

                lineTo(offset.x + cornerRadius, offset.y + size.height)

                if (Corner.BottomLeft in effectiveCornersToRound) {
                    arcTo(
                        rect = Rect(
                            left = offset.x,
                            top = offset.y + size.height - 2 * cornerRadius,
                            right = offset.x + 2 * cornerRadius,
                            bottom = offset.y + size.height
                        ),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                } else {
                    lineTo(offset.x, offset.y + size.height)
                    lineTo(offset.x, offset.y + size.height - cornerRadius)
                }

                lineTo(offset.x, offset.y + cornerRadius)

                if (Corner.TopLeft in effectiveCornersToRound) {
                    arcTo(
                        rect = Rect(
                            left = offset.x,
                            top = offset.y,
                            right = offset.x + 2 * cornerRadius,
                            bottom = offset.y + 2 * cornerRadius
                        ),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                } else {
                    lineTo(offset.x, offset.y)
                    lineTo(offset.x + cornerRadius, offset.y)
                }

                close()
            }

            canvas.drawPath(path, paint)

            canvas.nativeCanvas.restore()
        } finally {
            canvas.nativeCanvas.restoreToCount(checkpointCount)
        }
    }
}