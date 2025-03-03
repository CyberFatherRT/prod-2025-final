package ru.prodcontest.booq.presentation.map

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.presentation.map.components.FloatingBackIcon
import kotlin.math.roundToInt

@Serializable
object MapScreenDestination

data class Point(
    val x: Int,
    val y: Int
) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
    operator fun minus(other: Point) = Point(x - other.x, y - other.y)
    fun toOffset() = Offset(x * CELL, y * CELL)
}

data class Space(
    val width: Int,
    val height: Int,
    val items: List<SpaceItem>
)

data class SpaceItem(
    val basePoint: Point,
    val offsets: List<Point>,
    var selected: Boolean = false,
    val name: String
) {
    fun massCenter() =
        Offset(
            (offsets.sumOf { it.x }.toFloat() / offsets.size + basePoint.x) * CELL + CELL / 2,
            (offsets.sumOf { it.y }.toFloat() / offsets.size + basePoint.y) * CELL + CELL / 2
        )
}

const val CELL = 80f

val AnimatableOffsetSaver = Saver<Animatable<Offset, AnimationVector2D>, Pair<Float, Float>>(
    save = { Pair(it.value.x, it.value.y) },
    restore = { Animatable(Offset(it.first, it.second), Offset.VectorConverter) }
)
val AnimatableFloatSaver = Saver<Animatable<Float, AnimationVector1D>, Float>(
    save = { it.value },
    restore = { Animatable(it) }
)

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

val FloatingRangeSaver = Saver<ClosedFloatingPointRange<Float>, Pair<Float, Float>>(
    save = {
        Pair(it.start, it.endInclusive)
    },
    restore = {
        it.first..it.second
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
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

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var sliderPosition by rememberSaveable(stateSaver = FloatingRangeSaver) { mutableStateOf(0f..1379f) }

    val density = LocalDensity.current
    val bottomInset =
        with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }
    val topInset =
        with(density) { WindowInsets.statusBars.getTop(density).toDp() }

    val leftBound by remember {
        derivedStateOf {
            var m = (sliderPosition.start.rem(60) / 15).roundToInt() * 15
            var h = (sliderPosition.start / 60).roundToInt()
            if (m == 60) {
                m = 0
                h += 1
            }
            if (h == 24) {
                m = 0
                h = 0
            }
            "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
        }
    }
    val rightBound by remember {
        derivedStateOf {
            var m = (sliderPosition.endInclusive.rem(60) / 15).roundToInt() * 15
            var h = (sliderPosition.endInclusive / 60).roundToInt()
            if (m == 60) {
                m = 0
                h += 1
            }
            if (h == 24) {
                m = 0
                h = 0
            }
            "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
        }
    }

    val meow by remember {
        mutableStateOf(
            Space(
                width = 30,
                height = 40,
                items = listOf(
                    SpaceItem(Point(0, 0), offsets = listOf(Point(0, 0)), name = "place 11"),
                    SpaceItem(
                        Point(10, 0),
                        offsets = listOf(Point(0, 0), Point(1, 0), Point(0, 1)), name = "place 12"
                    ),
                    SpaceItem(
                        Point(0, 10),
                        offsets = listOf(Point(0, 0), Point(0, 1), Point(0, 2)),
                        name = "place 13"
                    ),
                    SpaceItem(
                        Point(5, 5),
                        offsets = listOf(
                            Point(0, 0),
                            Point(0, 1),
                            Point(0, 2),
                            Point(0, 3),
                            Point(0, 4),
                            Point(1, 0),
                            Point(1, 1),
                            Point(1, 2),
                            Point(1, 3),
                            Point(1, 4),
                            Point(1, 5)
                        ),
                        name = "place 14"
                    )
                )
            )
        )
    }

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

    fun focusToItem(item: SpaceItem, canvasSize: IntSize) {
        cameraAnimationScope.launch {
            camera.animateTo(
                Offset(
                    item.massCenter().x - canvasSize.width / 4,
                    item.massCenter().y - canvasSize.height / 4 + canvasSize.height / 8
                ), animationSpec = tween(1000)
            )
        }
        cameraScalingAnimationScope.launch {
            cameraScaling.animateTo(2f, animationSpec = tween(1000))
        }
    }

    var selectedName by remember { mutableStateOf("") }
    var canvasSize by remember { mutableStateOf(Size.Unspecified) }

    var coworkingDropdownOpened by remember { mutableStateOf(false) }


    Box(
        Modifier
            .fillMaxSize()
    ) {

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
                        meow.items.forEach { item ->
                            item.offsets.forEach { dot ->
                                val a = (dot + item.basePoint).toOffset()
                                if (point.x >= a.x && point.y >= a.y && point.x <= a.x + CELL && point.y <= a.y + CELL) {
                                    item.selected = !item.selected
                                    invalidations++
                                    Log.d("MEOW", "$item")
//                                showBottomSheet = true
                                    selectedName = item.name

                                    focusToItem(item, size)
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
                drawLine(Color.Gray, Offset(0f, 0f), Offset(meow.width * CELL, 0f))
                drawLine(Color.Gray, Offset(0f, 0f), Offset(0f, meow.height * CELL))
                drawLine(
                    Color.Gray,
                    Offset(meow.width * CELL, 0f),
                    Offset(meow.width * CELL, meow.height * CELL)
                )
                drawLine(
                    Color.Gray,
                    Offset(0f, meow.height * CELL),
                    Offset(meow.width * CELL, meow.height * CELL)
                )
                (0..meow.width).forEach { xCell ->
                    if (xCell != meow.width) {
                        (1..4).forEach { offset ->
                            drawLine(
                                Color.Gray,
                                Offset(xCell * CELL + offset * (CELL / 4), 0f),
                                Offset(xCell * CELL + offset * (CELL / 4), meow.height * CELL),
                                alpha = 0.5f,
                                strokeWidth = 0.5f
                            )
                        }
                    }
                    drawLine(
                        Color.Gray,
                        Offset(xCell * CELL, 0f),
                        Offset(xCell * CELL, meow.height * CELL),
                        strokeWidth = 2f,
                        alpha = 0.9f
                    )
                }
                (0..meow.height).forEach { yCell ->
                    if (yCell != meow.height) {
                        (1..4).forEach { offset ->
                            drawLine(
                                Color.Gray,
                                Offset(0f, yCell * CELL + offset * (CELL / 4)),
                                Offset(meow.width * CELL, yCell * CELL + offset * (CELL / 4)),
                                alpha = 0.5f,
                                strokeWidth = 0.5f
                            )
                        }
                    }
                    drawLine(
                        Color.Gray,
                        Offset(0f, yCell * CELL),
                        Offset(meow.width * CELL, yCell * CELL),
                        strokeWidth = 2f,
                        alpha = 0.9f
                    )
                }

                meow.items.forEach { item ->
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
                            if (item.selected) Color.White else Color.Green,
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
                                val rectOffset = actualDot.toOffset()
                                val rectSize = Size(CELL + 2f, CELL + 2f)

                                val cornersToRound = mutableListOf<Corner>()

                                if (hasTopRight) cornersToRound.add(Corner.TopRight)
                                if (hasTopLeft) cornersToRound.add(Corner.TopLeft)
                                if (hasBottomRight) cornersToRound.add(Corner.BottomRight)
                                if (hasBottomLeft) cornersToRound.add(Corner.BottomLeft)

                                drawRectWithCustomCorners(
                                    color = if (item.selected) Color.White else Color.Green,
                                    offset = rectOffset,
                                    size = rectSize,
                                    cornerRadius = CELL / 4,
                                    cornersToRound = cornersToRound,
                                    cornersToKeepSquare = emptyList()
                                )
                            }
                        }
                    }

//                val topLeftBoundX = item.offsets.minBy { it.x }.x
//                val topLeftBoundY = item.offsets.minBy {it.y}.y
//                val botRightBoundX = item.offsets.maxBy { it.x }.x
//                val botRightBoundY = item.offsets.maxBy { it.y }.y
//                (topLeftBoundX..botRightBoundX).forEach { xCell ->
//                    (topLeftBoundY..botRightBoundY).forEach { yCell ->
//                        if (
//                            item.offsets.any { it.x == xCell + 1 && it.y == yCell } and
//                            item.offsets.any { it.x == xCell && it.y == yCell - 1 } and
//                            item.offsets.none { it.x == xCell - 1 && it.y == yCell } and
//                            item.offsets.none { it.x == xCell && it.y == yCell + 1 }
//                        ) {
//                            val actualDot = item.basePoint + Point(xCell, yCell)
//                            drawRect(if (item.selected) Color.White else Color.Green, actualDot.toOffset(), Size(CELL+2f, CELL +2f))
//                            drawRoundRect(Color.Gray, actualDot.toOffset(), Size(CELL+2f, CELL +2f), CornerRadius(CELL/4,CELL/4))
//                        }
//                    }
//                }


                }
            }
        }
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .offset(
                    0.dp, bottomInset
                )
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Доступные с $leftBound до $rightBound",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(10.dp, 6.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("21.02.2025", style = MaterialTheme.typography.labelLarge)

                        IconButton(
                            onClick = {

                            },
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Icon(Icons.Default.DateRange, null)
                        }
                    }
                }
                RangeSlider(
                    value = sliderPosition,
                    onValueChange = { range -> sliderPosition = range },
                    onValueChangeFinished = {
                        Log.d("MEOW", sliderPosition.toString())
                    },
                    modifier = Modifier.padding(16.dp, 8.dp),
                    valueRange = 0f..1379f,
                )
                Spacer(Modifier.height(bottomInset + 4.dp))
            }
        }
        FloatingBackIcon(onClick = {})

        ExposedDropdownMenuBox(
            expanded = coworkingDropdownOpened,
            onExpandedChange = { coworkingDropdownOpened = !coworkingDropdownOpened },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset((-18).dp, 38.dp)
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.98f)
                    )
                    .padding(8.dp, 2.dp, 8.dp, 8.dp),
            ) {
                OutlinedTextField(
                    "CWRK 1",
                    readOnly = true,
                    onValueChange = {},
                    label = { Text("Выбранный коворкинг") },
                    trailingIcon = {
                        if (coworkingDropdownOpened) {
                            Icon(Icons.Default.KeyboardArrowUp, null)
                        } else {
                            Icon(Icons.Default.KeyboardArrowDown, null)
                        }
                    },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor =
                            OutlinedTextFieldDefaults.colors().focusedLabelColor,
                        unfocusedLabelColor = OutlinedTextFieldDefaults.colors().focusedLabelColor
                    )
                )
            }
            ExposedDropdownMenu(
                expanded = coworkingDropdownOpened,
                onDismissRequest = { coworkingDropdownOpened = false },
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                DropdownMenuItem(
                    text = { Text("meow") },
                    onClick = {}
                )
                DropdownMenuItem(
                    text = { Text("meow") },
                    onClick = {}
                )
                DropdownMenuItem(
                    text = { Text("meow") },
                    onClick = {}
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
            Text(selectedName)
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