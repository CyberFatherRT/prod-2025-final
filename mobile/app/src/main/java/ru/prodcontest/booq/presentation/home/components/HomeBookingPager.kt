package ru.prodcontest.booq.presentation.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.prodcontest.booq.presentation.theme.BooqTheme
import kotlin.math.absoluteValue

@Composable
fun HomeBookingPager(
    bookings: List<BookingDataUi>,
    onBookingClick: (Int) -> Unit,
    onBookingEditClick: (Int) -> Unit,
    onQRClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        bookings.size
    }

    val haptic = LocalHapticFeedback.current
    var previousPage by remember { mutableStateOf(pagerState.currentPage) }

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 25.dp),
        pageSize = PageSize.Fill,
        modifier = modifier.fillMaxWidth()
    ) { page ->
        Box(
            modifier = Modifier
                .graphicsLayer {
                    val pageOffset = (
                            (pagerState.currentPage - page) + pagerState
                                .currentPageOffsetFraction
                            ).absoluteValue

                    lerp(
                        start = 0.9f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    ).also { scale ->
                        scaleX = scale
                        scaleY = scale
                    }

                    alpha = lerp(
                        start = 0.5f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                }
                .fillMaxWidth()
                .padding(horizontal = 0.dp)
        ) {
            HomeBookingCard(
                data = bookings[page],
                onBookingClick = { onBookingClick(page) },
                onBookingEditClick = { onBookingEditClick(page) },
                onDeleteClick = { onDeleteClick(page) },
                onQRClick = { onQRClick(page) }
            )
        }
    }
    
    LaunchedEffect(pagerState.currentPage, pagerState.currentPageOffsetFraction) {
        if (pagerState.currentPage != previousPage &&
            pagerState.currentPageOffsetFraction.absoluteValue < 0.05f) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            previousPage = pagerState.currentPage
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

@Preview
@Composable
fun HomeBookingPagerPreview() {
    val sampleBookings = listOf(
        BookingDataUi(
            name = "K-254",
            label = "- тянки\n- монитор\n- очень тихо",
            address = "Москва, ул. Ленина, д. 1\nИгровая комната 3",
            date = "13:00-15:00 12.12.2021",
            space = "Подтверждено"
        ),
        BookingDataUi(
            name = "K-255",
            label = "- 2 монитора\n- тихо",
            address = "Москва, ул. Ленина, д. 1\nИгровая комната 4",
            date = "15:00-17:00 12.12.2021",
            space = "В ожидании"
        ),
        BookingDataUi(
            name = "K-256",
            label = "- игровой ПК\n- VR",
            address = "Москва, ул. Ленина, д. 1\nИгровая комната 5",
            date = "17:00-19:00 12.12.2021",
            space = "Подтверждено"
        )
    )

    BooqTheme {

            HomeBookingPager(
                bookings = sampleBookings,
                onBookingClick = {},
                onBookingEditClick = {},
                onQRClick = {},
                onDeleteClick = {},
                modifier = Modifier.padding(vertical = 16.dp)
            )

    }
}