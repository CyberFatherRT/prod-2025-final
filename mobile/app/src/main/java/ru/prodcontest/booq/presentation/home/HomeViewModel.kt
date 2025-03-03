package ru.prodcontest.booq.presentation.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.domain.model.BookingCompanyModel
import ru.prodcontest.booq.domain.model.BookingIdData
import ru.prodcontest.booq.domain.model.BookingModel
import ru.prodcontest.booq.domain.model.BookingTextModel
import ru.prodcontest.booq.domain.model.BookingTime
import ru.prodcontest.booq.domain.repository.ApiRepository
import ru.prodcontest.booq.domain.usecase.GetTokenUseCase
import ru.prodcontest.booq.domain.util.ResultWrapper
import ru.prodcontest.booq.presentation.BaseViewModel
import java.time.LocalDateTime
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiRepository: ApiRepository,
    private val GetTokenUseCase: GetTokenUseCase
): BaseViewModel<HomeState, HomeScreenEvent>() {

    override fun setInitialState() = HomeState(
        bookings = emptyList(),
        isLoading = true,
        error = null
    )

    init {
        checkToken()
        getBookings()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getBookings() = viewModelScope.launch {
        apiRepository.getBookingList().collect {
            Log.d("SSS", it.toString())
            when (it) {
                is ResultWrapper.Ok -> {
                    setState { copy(bookings = it.data, isLoading = false, error = null) }

                    if (it.data.isEmpty()) {
                        val bookingExample = BookingModel(
                            idData = BookingIdData(
                                id = "K-123",
                                companyId = "company-456",
                                itemId = "item-789",
                                spaceId = "space-101",
                                userId = "user-202"
                            ),
                            company = BookingCompanyModel(
                                id = "D-456",
                                name = "Tech Solutions Ltd.",
                                address = "123 Main Street, Tech City"
                            ),
                            name = BookingTextModel(
                                id = "A-123",
                                label = "Conference Room Booking",
                                company = "Tech Solutions Ltd.",
                                item = "Projector",
                                space = "Main Conference Room"
                            ),
                            time = BookingTime(
                                start = LocalDateTime.of(2025, 3, 3, 10, 0),
                                end = LocalDateTime.of(2025, 3, 3, 12, 0)
                            )
                        )
                        setState {
                            val bookings: List<BookingModel> = listOf(bookingExample, bookingExample, bookingExample)
                            copy(bookings=bookings, isLoading = false, error = null)
                        }
                    }
                }

                is ResultWrapper.Loading -> {
                    setState { copy(bookings = emptyList(), isLoading = true, error = null) }
                }

                is ResultWrapper.Error -> {
                    if (("No authorization token was found" in it.message) or ("JWT error" in it.message)) {
                        setAction { HomeScreenEvent.NavigateToLoginScreen }
                    }
                    setState { copy(bookings = emptyList(), isLoading = false, error = it.message) }
                }
            }
        }
    }


    private fun checkToken() {
        if (GetTokenUseCase().isNullOrEmpty()) {
            setAction { HomeScreenEvent.NavigateToLoginScreen }
        }
        Log.d("SSS", GetTokenUseCase().toString())
    }
}

sealed class HomeScreenEvent {
    data class OpenQr(val bookingId: String): HomeScreenEvent()
    data object NavigateToLoginScreen: HomeScreenEvent()
    data class ShowError(val message: String) : HomeScreenEvent()
}