package ru.prodcontest.booq.presentation.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
        qrCode = QrCodeInfo(token = "", state = QrCodeState.Loading),
        error = null
    )

    init {
        checkToken()
        getBookings()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getBookings() = viewModelScope.launch {
        apiRepository.getBookingList().collect {
            when (it) {
                is ResultWrapper.Ok -> {
                    setState { copy(bookings = it.data, isLoading = false, error = null) }

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

    fun getQr(bookingId: String) {

//        setState { copy(qrCode = QrCodeInfo(token = "", state = QrCodeState.Loading)) }
//
        viewModelScope.launch {
            apiRepository.getQr(bookingId).collect {
                when (it) {
                    is ResultWrapper.Ok -> {
                        setState {
                            copy(
                                qrCode = QrCodeInfo(
                                    token = it.data.token,
                                    state = QrCodeState.Ok
                                )
                            )
                        }
                    }

                    is ResultWrapper.Loading -> {
                        setState {
                            copy(
                                qrCode = QrCodeInfo(
                                    token = "",
                                    state = QrCodeState.Loading
                                )
                            )
                        }
                    }

                    is ResultWrapper.Error -> {
                        setState {
                            copy(
                                qrCode = QrCodeInfo(
                                    token = "",
                                    state = QrCodeState.Error(it.message)
                                )
                            )
                        }
                    }
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
    data object NavigateToLoginScreen: HomeScreenEvent()
    data class ShowError(val message: String) : HomeScreenEvent()
}