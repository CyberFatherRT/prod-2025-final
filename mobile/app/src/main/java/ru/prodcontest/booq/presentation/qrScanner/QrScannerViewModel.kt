package ru.prodcontest.booq.presentation.qrScanner

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.prodcontest.booq.domain.repository.ApiRepository
import ru.prodcontest.booq.domain.util.ResultWrapper
import ru.prodcontest.booq.presentation.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class QrScannerViewModel @Inject constructor(
    private val apiRepository: ApiRepository
)  : BaseViewModel<QrScannerScreenState, Nothing>() {
    override fun setInitialState() = QrScannerScreenState(
        loadingVerdict = false,
        errorVerdict = null,
        verdict = null,
        modalOpened = false
    )

    fun checkQr(token: String) {
        setState { copy(modalOpened = true) }
        viewModelScope.launch {
            apiRepository.verifyBookingQr(token).onEach {
                when(it) {
                    is ResultWrapper.Ok -> {setState { copy(loadingVerdict = false, errorVerdict = null, verdict = it.data) }}
                    is ResultWrapper.Error -> {setState { copy(loadingVerdict = false, errorVerdict = it.message, verdict = null) }}
                    ResultWrapper.Loading -> {setState { copy(loadingVerdict = true, errorVerdict = null, verdict = null) }}
                }
            }.collect()
        }
    }
    fun closeModal() = setState { copy(modalOpened = false) }
}