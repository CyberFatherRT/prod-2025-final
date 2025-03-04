package ru.prodcontest.booq.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer

@Preview()
@Composable
fun HomeBookingCardShimmer(
    modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .clip(
                    shape = RoundedCornerShape(32.dp)
                )
                .background(
                    color = Color.Gray.copy(alpha = 0.2f)
                )
                .width(360.dp)
                .height(474.dp)
                .shimmer()
        ) {}

    }