package ru.prodcontest.booq.presentation.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.R
import ru.prodcontest.booq.domain.model.BookingModel
import ru.prodcontest.booq.domain.model.BookingTime
import ru.prodcontest.booq.presentation.auth.login.LoginScreenDestination
import ru.prodcontest.booq.presentation.home.components.BookingDataUi
import ru.prodcontest.booq.presentation.home.components.HomeBookingCardShimmer
import ru.prodcontest.booq.presentation.home.components.HomeBookingPager
import ru.prodcontest.booq.presentation.home.components.HomeRegisterCompanyDialog
import ru.prodcontest.booq.presentation.home.components.QRCodeDialog
import ru.prodcontest.booq.presentation.home.components.QRCodeDialogUiModel
import ru.prodcontest.booq.presentation.map.MapScreenDestination
import ru.prodcontest.booq.presentation.profile.ProfileScreenDestination
import ru.prodcontest.booq.presentation.qrScanner.QrScannerScreenDestination
import ru.prodcontest.booq.presentation.selectBuilding.SelectBuildingScreenDestination
import ru.prodcontest.booq.presentation.verifications.VerificationsScreenDestination
import java.time.format.DateTimeFormatter

@Serializable
object HomeScreenDestination {
    const val route = "home_screen"
    const val companyNameArg = "companyName"
    const val routeWithArgs = "$route?companyName={$companyNameArg}"
    val arguments = listOf(
        navArgument(companyNameArg) {
            type = NavType.StringType
            defaultValue = ""
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    firstRegisterCompany: String = "",
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val actionsScope = rememberCoroutineScope()

    var showCompanyDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    var selectedBookingIndex = remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        actionsScope.launch {
            viewModel.action.collect { action ->
                when (action) {
                    is HomeScreenEvent.NavigateToLoginScreen -> {
                        navController.navigate(LoginScreenDestination)
                    }

                    is HomeScreenEvent.ShowError -> {
                        snackbarHostState.showSnackbar(action.message)
                    }
                }
            }
        }
    }

    val viewState = viewModel.viewState.value
    val bookings = viewState.bookings

    var adminDialogOpened by remember { mutableStateOf(false) }

    if(adminDialogOpened) {
        AlertDialog(
            onDismissRequest = {adminDialogOpened = false},
            title = {Text("Инструменты администратора")},
            text = {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Button({ navController.navigate(QrScannerScreenDestination) }) { Text("Проверка QR кодов") }
                    Button({ navController.navigate(VerificationsScreenDestination)}) { Text("Верификация пользователей") }
                }
            },
            dismissButton = {
                TextButton({adminDialogOpened = false}) {
                    Text("Закрыть")
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if(viewState.isAdmin) {
                        IconButton({adminDialogOpened = true}) {
                            Icon(Icons.Default.Build, null)
                        }
                    }
                },
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Бронирование",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(ProfileScreenDestination) }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Профиль"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.background(Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Забронировать") },

                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить бронирование"
                    )
                },
                onClick = {
                    if ((viewState.isLoading and !viewState.isProcessDelete) or (viewState.error != null) or viewState.isUnverified) null else navController.navigate(
                        SelectBuildingScreenDestination
                    )
                },
                containerColor = if ((viewState.isLoading and !viewState.isProcessDelete) or (viewState.error != null) or viewState.isUnverified) Color.Gray else MaterialTheme.colorScheme.primary
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {

            if (firstRegisterCompany.isNotEmpty()) {
                showCompanyDialog = true
                HomeRegisterCompanyDialog(
                    onDismissRequest = {
                        navController.navigate(HomeScreenDestination.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    comapnyId = firstRegisterCompany,
                    modifier = Modifier
                        .padding(12.dp),
                )
            }

            if (viewState.error != null && ("No address associated with hostname" in viewState.error)) {
                Log.d("asd", viewState.error)
                ShowInfoElement(
                    text = "Отсутствует интернет!",
                    iconId = R.drawable.wifi_off_24
                )

            } else if (viewState.error != null) {
                ShowInfoElement(
                    text = "Возникла ошибка:\n\n ${viewState.error}",
                    iconId = R.drawable.warning_24
                )
            } else if (viewState.isLoading) {
                HomeBookingCardShimmer()
            } else if (viewState.bookings.isEmpty() && !viewState.isLoading) {
                ShowInfoElement(
                    text = "Бронирования отсутствуют",
                    iconId = R.drawable.receipt_long_24
                )
            } else if (viewState.bookings.isNotEmpty()) {
                HomeBookingPager(
                    bookings = bookings.map { it.toHomeScreenUiModel() },
                    onBookingClick = { index -> "Клик на редактирование" },
                    onBookingEditClick = { index ->
                        bookings.getOrNull(selectedBookingIndex.value)?.let { booking ->
                            navController.navigate(
                                MapScreenDestination(
                                    buildingId = null,
                                    bookingId = booking.idData.id,
                                    coworkingItemId = booking.idData.itemId,
                                    coworkingsSpaceId = booking.idData.spaceId
                                )
                            )
                        }
                    },
                    onQRClick = { index ->
                        selectedBookingIndex.value = index

                        val bookingId = bookings.getOrNull(selectedBookingIndex.value)?.idData?.id

                        if (bookingId != null) {
                            viewModel.getQr(bookingId)
                        } else {
                            Log.e("HomeScreen", "Error while getting QR code: booking data is null")
                        }
                        showDialog = true

                    },
                    onDeleteClick = { index ->
                        selectedBookingIndex.value

                        val bookingId = bookings.getOrNull(selectedBookingIndex.value)?.idData?.id

                        if (bookingId != null) {
                            viewModel.deleteBooking(bookingId)
                        } else {
                            Log.e("HomeScreen", "Error click delete")
                        }

                    }
                )
            }

            if (viewState.error == null && !viewState.isLoading && showDialog) {
                val data = bookings.getOrNull(selectedBookingIndex.value)?.toQRCodeDialogUiModel()

                if (data != null) {

                    QRCodeDialog(
                        data = data,
                        qrCodeText = viewState.qrCode.token,
                        onDismissRequest = {
                            showDialog = false

                        },
                        modifier = Modifier
                            .padding(12.dp),
                    )
                } else {
                    Log.e("HomeScreen", "Error while showing QR code dialog: booking data is null")
                }
            }
        }
    }
}

@Composable
private fun ShowInfoElement(
    text: String,
    iconId: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier
                .size(42.dp),
            contentDescription = null,
            painter = painterResource(iconId),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            textAlign = TextAlign.Center
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun BookingModel.toHomeScreenUiModel(): BookingDataUi {
    return BookingDataUi(
        name = name.id,
        label = name.label,
        address = company.address,
        date = time.formatBookingTime(),
        space = name.space + " " + name.item
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun BookingTime.formatBookingTime(): String {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy")

    val isSameDay = start.toLocalDate() == end.toLocalDate()

    return if (isSameDay) {
        val timeRange = "${start.format(timeFormatter)}-${end.format(timeFormatter)}"
        val date = start.format(dateFormatter)
        "$timeRange $date"
    } else {
        val startStr = "${start.format(timeFormatter)} ${start.format(dateFormatter)}"
        val endStr = "${end.format(timeFormatter)} ${end.format(dateFormatter)}"
        "$startStr - $endStr"
    }
}

fun BookingModel.toQRCodeDialogUiModel(): QRCodeDialogUiModel {
    return QRCodeDialogUiModel(
        name = name.id,
        address = company.address,
        time = time.formatBookingTime(),
    )
}