package ru.prodcontest.booq.presentation.home

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.domain.model.BookingModel
import ru.prodcontest.booq.presentation.auth.login.LoginScreenAction
import ru.prodcontest.booq.presentation.auth.login.LoginScreenDestination
import ru.prodcontest.booq.presentation.home.components.BookingDataUi
import ru.prodcontest.booq.presentation.home.components.HomeBookingCard
import ru.prodcontest.booq.presentation.home.components.HomeBookingCardShimmer
import ru.prodcontest.booq.presentation.home.components.HomeBookingPager

@Serializable
object HomeScreenDestination

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val actionsScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        actionsScope.launch {
            viewModel.action.collect { action ->
                when(action) {
                    is HomeScreenEvent.OpenQr -> {
                        println("QRRRRRRRRRRRRRRRRRRRRRRR")
                    }
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {

        if (viewState.error != null) {
            Text("Произошла ошибочка: \n ${viewState.error}")
        } else if (viewState.isLoading) {
            HomeBookingCardShimmer()
        } else if (viewState.bookings.isEmpty() and !viewState.isLoading) {
            Text("Тут пока пусто.")
        } else if (viewState.bookings.isNotEmpty()) {
            val testEl = viewState.bookings[0]


            HomeBookingPager(
                bookings = bookings.map { it.toHomeScreenUiModel() },
                onBookingClick = { index ->
                    Log.d("BookingClick", "Clicked on booking at index: $index with id: ${bookings[index]}")
                                 },
                onBookingEditClick = { index -> "Клик на редактирование" },
                onQRClick = { index -> "Клик на QR" }
            )
        }
    }

}


fun BookingModel.toHomeScreenUiModel(): BookingDataUi {
    return BookingDataUi(
        name = name.id,
        label = name.label,
        address = company.address,
        date = time.start.toString(),
        status = "Переделать"
    )
}

