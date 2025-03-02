package ru.prodcontest.booq.presentation.map

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.math.sqrt

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
)

const val CELL = 80f

val AnimatableOffsetSaver = Saver<Animatable<Offset, AnimationVector2D>, Pair<Float, Float>>(
    save = { Pair(it.value.x, it.value.y) },
    restore = { Animatable(Offset(it.first, it.second), Offset.VectorConverter) }
)
val AnimatableFloatSaver = Saver<Animatable<Float, AnimationVector1D>, Float>(
    save = { it.value },
    restore = { Animatable(it) }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
//    var camera by remember { mutableStateOf(Offset(10f, 50f)) }
//    val cameraTransX = remember { Animatable(0f) }
//    val cameraTransY = remember { Animatable(0f) }
    val camera = rememberSaveable(saver = AnimatableOffsetSaver) {
        Animatable(
            Offset.Zero,
            Offset.VectorConverter
        )
    }
    val cameraScaling = rememberSaveable(saver = AnimatableFloatSaver) { Animatable(1f) }
    val cameraAnimationScope = rememberCoroutineScope()
    val cameraScalingAnimationScope = rememberCoroutineScope()

    val ctx = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var invalidations by remember { mutableIntStateOf(0) }

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

    val scalingLimiter = { value: Float ->
        1 / (-10 * value - 5) + 2
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

    LaunchedEffect(Unit) {
        Toast.makeText(ctx, "asdasdasd", Toast.LENGTH_SHORT).show()
    }
    val mtoast = { txt: String ->
        Toast.makeText(ctx, txt, Toast.LENGTH_SHORT).show()
    }

    var selectedName by remember { mutableStateOf("") }

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
                                showBottomSheet = true
                                selectedName = item.name

//                                val centroid = Offset(it.x, it.y)
//                                val oldScale = cameraScaling.value
//                                val newScale = sqrt(cameraScaling.value) + 0.5f
//                                Log.d("MEOW1", "$oldScale")
//                                cameraAnimationScope.launch {
//                                    camera.animateTo(
//                                        (camera.value + centroid / oldScale) - (centroid / newScale),
//                                        animationSpec = tween(1000)
//                                    )
//                                }
//                                cameraScalingAnimationScope.launch {
//                                    cameraScaling.animateTo(newScale, animationSpec = tween(1000))
//                                }
                                zoomTo(
                                    it,
                                    Offset.Zero,
                                    sqrt(cameraScaling.value) + 0.5f,
                                    tween(1000),
                                    tween(1000)
                                )
                            }
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    Log.d("MEOW", "$pan")
//                    val oldScale = cameraScaling.value
//                    val newScale = cameraScaling.value * zoom
//                    cameraAnimationScope.launch {
//                        camera.animateTo(
//                            (camera.value + centroid / oldScale) - (centroid / newScale + pan / oldScale),
//                            animationSpec = snap(0)
//                        )
//                    }
//                    cameraScalingAnimationScope.launch {
//                        cameraScaling.animateTo(newScale, animationSpec = snap(0))
//                    }
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
                    drawRect(
                        if (item.selected) Color.White else Color.Green,
                        Offset(actualDot.x * CELL - 2f, actualDot.y * CELL - 2f),
                        Size(CELL + 2f, CELL + 2f)
                    )
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
            Text(selectedName)
        }
    }
}