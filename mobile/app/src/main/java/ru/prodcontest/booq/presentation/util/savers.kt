package ru.prodcontest.booq.presentation.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.geometry.Offset

val AnimatableOffsetSaver = Saver<Animatable<Offset, AnimationVector2D>, Pair<Float, Float>>(
    save = { Pair(it.value.x, it.value.y) },
    restore = { Animatable(Offset(it.first, it.second), Offset.VectorConverter) }
)
val AnimatableFloatSaver = Saver<Animatable<Float, AnimationVector1D>, Float>(
    save = { it.value },
    restore = { Animatable(it) }
)
val FloatingRangeSaver = Saver<ClosedFloatingPointRange<Float>, Pair<Float, Float>>(
    save = {
        Pair(it.start, it.endInclusive)
    },
    restore = {
        it.first..it.second
    }
)