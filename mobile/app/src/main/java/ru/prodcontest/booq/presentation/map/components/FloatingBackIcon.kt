package ru.prodcontest.booq.presentation.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun BoxScope.FloatingBackIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val topInset =
        with(density) { WindowInsets.statusBars.getTop(density).toDp() }

    Box(
        modifier = Modifier
            .offset(12.dp, 16.dp + topInset)
            .clip(CircleShape)
            .size(48.dp)
            .background(
                MaterialTheme.colorScheme.surfaceContainer
            )
            .clickable {
                onClick()
            }
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            null,
            modifier = Modifier.align(
                Alignment.Center
            )
        )
    }
}