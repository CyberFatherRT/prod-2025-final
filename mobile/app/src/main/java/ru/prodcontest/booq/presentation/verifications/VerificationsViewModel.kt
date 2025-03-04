package ru.prodcontest.booq.presentation.verifications

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.prodcontest.booq.domain.repository.ApiRepository
import ru.prodcontest.booq.domain.util.ResultWrapper
import ru.prodcontest.booq.domain.util.UploadProgress
import ru.prodcontest.booq.presentation.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class VerificationsViewModel @Inject constructor(
    private val apiRepository: ApiRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel<VerificationsScreenState, ProfileScreenAction>() {
    override fun setInitialState() = VerificationsScreenState(
        verificationsInfo = null,
        isLoading = true,
        error = null,
    )

    init {
        getVerificationsInfo()
    }

    private fun getVerificationsInfo() = viewModelScope.launch {
        apiRepository.getVerifications().onEach {
            when (it) {
                is ResultWrapper.Ok -> {
                    setState { copy(verificationsInfo = it.data, isLoading = false, error = null) }
                }

                is ResultWrapper.Error -> {
                    setState { copy(verificationsInfo = null, isLoading = false, error = it.message) }
                }

                ResultWrapper.Loading -> {
                    setState { copy(verificationsInfo = null, isLoading = true, error = null) }
                }
            }
        }.collect()
    }

    fun approveUser(userId: String) {
        Log.d("MEOW", userId)

        viewModelScope.launch {
            apiRepository.verifyGuest(userId).collect {
                when(it) {
                    is ResultWrapper.Error -> {}
                    ResultWrapper.Loading -> {}
                    is ResultWrapper.Ok -> {}
                }
            }
        }
    }

    fun declineUser(userId: String) {
        Log.d("MEOW", userId)

        viewModelScope.launch {
            apiRepository.declineGuest(userId).collect {
                when(it) {
                    is ResultWrapper.Error -> TODO()
                    ResultWrapper.Loading -> TODO()
                    is ResultWrapper.Ok -> TODO()
                }
            }
        }
    }
}

sealed class ProfileScreenAction {
    data class ShowError(val message: String) : ProfileScreenAction()
}