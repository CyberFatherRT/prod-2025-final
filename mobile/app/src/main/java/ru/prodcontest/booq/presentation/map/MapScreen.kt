package ru.prodcontest.booq.presentation.map

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.presentation.map.components.FloatingBackIcon
import ru.prodcontest.booq.presentation.util.FloatingRangeSaver

@Serializable
data class MapScreenDestination(
    val buildingId: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val state = viewModel.viewState.value

    var sliderPosition by rememberSaveable(stateSaver = FloatingRangeSaver) { mutableStateOf(0f..1379f) }
    val snackbarHostState = remember { SnackbarHostState() }

    val density = LocalDensity.current
    val bottomInset =
        with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }

    val leftBound by remember { derivedStateOf { minuteToTime(sliderPosition.start) } }
    val rightBound by remember { derivedStateOf { minuteToTime(sliderPosition.endInclusive) } }
    var coworkingDropdownOpened by remember { mutableStateOf(false) }

    val actionsScope = rememberCoroutineScope()
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
                        onClickItem = {},
                        items = cowoData
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
            FloatingBackIcon(onClick = {})
        }
    }
}