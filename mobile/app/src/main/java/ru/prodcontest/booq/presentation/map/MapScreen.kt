package ru.prodcontest.booq.presentation.map

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.domain.model.BookingSpan
import ru.prodcontest.booq.presentation.home.HomeScreenDestination
import ru.prodcontest.booq.presentation.map.components.FloatingBackIcon
import ru.prodcontest.booq.presentation.util.FloatingRangeSaver
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import kotlin.math.roundToLong

@Serializable
data class MapScreenDestination(
    val buildingId: String? = null,
    val bookingId: String? = null,
    val coworkingItemId: String? = null,
    val coworkingsSpaceId: String? = null
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = hiltViewModel()
) {
    val state = viewModel.viewState.value

    var sliderPosition by rememberSaveable(stateSaver = FloatingRangeSaver) { mutableStateOf(0f..1379f) }
    val snackbarHostState = remember { SnackbarHostState() }

    val density = LocalDensity.current
    val locale = LocalConfiguration.current.locales.get(0)
    val bottomInset =
        with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }

    val leftBound by remember { derivedStateOf { minuteToTime(sliderPosition.start) } }
    val rightBound by remember { derivedStateOf { minuteToTime(sliderPosition.endInclusive) } }

    val selectSpan by remember {
        derivedStateOf {
            BookingSpan(
                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).plus(
                    sliderPosition.start.roundToLong(),
                    ChronoUnit.MINUTES
                ), LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).plus(
                    sliderPosition.endInclusive.roundToLong(),
                    ChronoUnit.MINUTES
                )
            )
        }
    }

    var coworkingDropdownOpened by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()

    val actionsScope = rememberCoroutineScope()
    val ctx = LocalContext.current
    LaunchedEffect(Unit) {
        actionsScope.launch {
            viewModel.action.collect { action ->
                when (action) {
                    is MapScreenAction.ShowLoadingCoworkingDataError -> {
                        snackbarHostState.showSnackbar(action.message)
                    }

                    is MapScreenAction.ShowLoadingCoworkingsError -> {
                        snackbarHostState.showSnackbar(action.message)
                    }
                    MapScreenAction.EndBooking -> {
                        navController.navigate(HomeScreenDestination.route) {
                            popUpTo(0)
                        }
                    }
                    is MapScreenAction.ShowBookingCreationError -> {
                        Toast.makeText(ctx, action.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { _ ->
        Box(
            Modifier
                .fillMaxSize()
        ) {
            state.coworkingData?.let { cowoData ->
                state.selectedCoworking?.let { cowo ->
                    CoworkingMap(
                        height = cowo.height,
                        width = cowo.width,
                        onClickItem = { viewModel.selectItem(it) },
                        items = cowoData,
                        selectedSpan = selectSpan
                    )
                }
            }
            if (state.isLoadingCoworkings || state.isCoworkingDataLoading) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                }
            }
            state.coworkings?.let { coworkings ->
                ExposedDropdownMenuBox(
                    expanded = coworkingDropdownOpened || state.selectedCoworking == null,
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
                            value = state.selectedCoworking?.address ?: "Выберите коворкинг",
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
                        coworkings.forEach { cowo ->
                            DropdownMenuItem(
                                text = { Text(cowo.address) },
                                onClick = { viewModel.selectCoworking(cowo) }
                            )
                        }
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
            FloatingBackIcon(onClick = { navController.navigateUp() })
        }
        if (state.bottomSheetOpened) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeBottomSheet() },
                sheetState = sheetState,
                modifier = Modifier.fillMaxHeight(),
                scrimColor = Color.DarkGray.copy(alpha = 0.1f)
            ) {
                Column(
                    Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    state.selectedItem?.let { item ->
                        Column(Modifier.padding(12.dp)) {
                            Text(item.name, style = MaterialTheme.typography.titleLarge)
                            Text(
                                item.description ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                        val now = LocalDateTime.now()
                        var selectedDay by remember { mutableStateOf(now) }
                        var startSlot by remember { mutableStateOf<Int?>(null) }
                        var endSlot by remember { mutableStateOf<Int?>(null) }

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .scrollable(rememberScrollState(), Orientation.Horizontal),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            (0..4).forEach { dayOffset ->
                                val thisDay = now.plus(
                                    dayOffset.toLong(),
                                    ChronoUnit.DAYS
                                )
                                Column(
                                    Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedDay = thisDay
                                        }
                                        .background(
                                            if (selectedDay.dayOfMonth == thisDay.dayOfMonth)
                                                MaterialTheme.colorScheme.tertiaryContainer
                                            else
                                                MaterialTheme.colorScheme.secondaryContainer
                                        )
                                        .width(48.dp)
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        thisDay.dayOfWeek.getDisplayName(
                                            TextStyle.SHORT_STANDALONE,
                                            locale
                                        ), style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        thisDay.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                        fun firstOccupationFor(hour: Int) = item.occupied.firstOrNull {
                            it.start.hour <= hour && (it.end.hour > hour || it.start.dayOfMonth != it.end.dayOfMonth) && it.start.dayOfMonth == selectedDay.dayOfMonth
                        }
                        OutlinedCard(Modifier.padding(8.dp, 12.dp)) {
                            (0..23).forEach { hour ->
                                val banned = now.isAfter(selectedDay.withHour(hour))
                                val backMod = if (banned) {
                                    Modifier.background(MaterialTheme.colorScheme.tertiaryContainer)
                                } else {
                                    Modifier
                                }
                                val firstOccupation = firstOccupationFor(hour)
                                Row(
                                    backMod
                                        .height(32.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            if (hour == startSlot) {
                                                startSlot = null
                                                endSlot = null
                                                Log.d("MEOW", "$startSlot $endSlot")
                                                return@clickable
                                            }
                                            if (startSlot == null) {
                                                if (firstOccupation == null && !banned) {
                                                    startSlot = hour
                                                    endSlot = hour
                                                }
                                            } else if (endSlot == startSlot) {
                                                if ((startSlot!!..hour).none { firstOccupationFor(it) != null }) {
                                                    endSlot = hour
                                                }
                                            } else {
                                                if (firstOccupation == null && !banned) {
                                                    startSlot = hour
                                                    endSlot = hour
                                                }
                                            }
                                            if (startSlot != null && endSlot != null) {
                                                if (startSlot!! > endSlot!!) {
                                                    startSlot = null
                                                    endSlot = null
                                                }
                                            }
                                            Log.d("MEOW", "$startSlot $endSlot")
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${hour.toString().padStart(2, '0')}:00",
                                        Modifier.padding(8.dp, 0.dp)
                                    )
                                    val dpPx = with(LocalDensity.current) { 1.dp.toPx() }
                                    Canvas(
                                        Modifier
                                            .height(32.dp)
                                            .offset((-64 - 24).dp, 0.dp), ""
                                    ) {
                                        firstOccupation?.let { occupation ->
                                            val yPoint = if (hour == occupation.start.hour) {
                                                occupation.start.minute * (32f / 60f)
                                            } else {
                                                0f
                                            }
                                            val ySize =
                                                32f - yPoint - if (hour == occupation.end.hour) {
                                                    (60f - occupation.end.minute) * (32f / 60f)
                                                } else {
                                                    0f
                                                }
//                                            val ySize1 = if(hour == occupation.end.hour)
                                            Log.d("MEOW", "$hour $yPoint $ySize $occupation")
                                            drawRect(
                                                Color(235, 61, 52),
                                                Offset(0f, dpPx * yPoint),
                                                Size(dpPx * 64f, dpPx * ySize)
                                            )
                                        }
                                        if (startSlot != null) {
                                            if (endSlot == null) {
                                                if (startSlot == hour) {
                                                    drawRect(
                                                        Color.Green,
                                                        Offset(72f, 0f),
                                                        Size(dpPx * 24f, dpPx * 32f)
                                                    )
                                                }
                                            } else {
                                                if (startSlot!! <= hour && endSlot!! >= hour) {
                                                    drawRect(
                                                        Color.Green,
                                                        Offset(72f, 0f),
                                                        Size(dpPx * 24f, dpPx * 32f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                        val notice = if (startSlot == null) {
                            "Выберите время начала бронирования"
                        } else if (endSlot == null) {
                            "Выберите время окончания бронирования"
                        } else if (endSlot == startSlot) {
                            "Выберите другое время окончания бронирования или продолжите"
                        } else {
                            "Продолжите или выберите новое время начала бронирования"
                        }
                        Text(
                            notice,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(12.dp)
                        )
                        startSlot?.let { ss ->
                            endSlot?.let { es ->
                                val startLDT = selectedDay.withHour(ss).withMinute(0).withSecond(0)
                                val endLDT = if (es == 23) {
                                    selectedDay.withHour(0).withMinute(0).withSecond(0).plus(
                                        1,
                                        ChronoUnit.DAYS
                                    )
                                } else {
                                    selectedDay.withHour(es + 1).withMinute(0).withSecond(0)
                                }
                                Text("Выбранное время: ${startLDT.hour.toString().padStart(2, '0')}:${startLDT.minute.toString().padStart(2, '0')} - ${endLDT.hour.toString().padStart(2, '0')}:${endLDT.minute.toString().padStart(2, '0')}", modifier = Modifier.padding(12.dp))

                                Button(
                                    onClick = { viewModel.confirmBooking(item, startLDT, endLDT) },
                                    enabled = !state.isCreatingBooking,
                                    modifier = Modifier.padding(12.dp).fillMaxWidth()
                                ) {
                                    Text("Подтвердить")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}