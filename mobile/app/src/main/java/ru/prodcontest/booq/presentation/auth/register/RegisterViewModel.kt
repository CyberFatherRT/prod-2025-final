package ru.prodcontest.booq.presentation.auth.register

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.prodcontest.booq.data.remote.dto.RegisterDto
import ru.prodcontest.booq.domain.repository.ApiRepository
import ru.prodcontest.booq.domain.usecase.SetTokenUseCase
import ru.prodcontest.booq.domain.util.ResultWrapper
import ru.prodcontest.booq.presentation.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val apiRepository: ApiRepository,
    private val setTokenUseCase: SetTokenUseCase
) : BaseViewModel<RegisterState, RegisterAction>() {
    override fun setInitialState() = RegisterState(
        isLoading = false,
        error = null
    )

    fun register(name: String, surname: String, email: String, password: String, domain: String) =
        viewModelScope.launch {
            apiRepository.register(RegisterDto(name, surname, email, password, domain)).onEach {
                when (it) {
                    is ResultWrapper.Ok -> {
                        setTokenUseCase(it.data.token)
                        setAction { RegisterAction.NavigateToHomeScreen }
                    }

                    ResultWrapper.Loading -> setState { copy(isLoading = true) }
                    is ResultWrapper.Error -> setState { copy(error = it.message) }
                }
            }.collect()
        }
}

sealed class RegisterAction {
    data object NavigateToHomeScreen : RegisterAction()
}