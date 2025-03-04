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
import ru.prodcontest.booq.presentation.auth.login.LoginScreenAction
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val apiRepository: ApiRepository,
    private val setTokenUseCase: SetTokenUseCase
) : BaseViewModel<RegisterState, RegisterAction>() {
    override fun setInitialState() = RegisterState(
        isLoading = false
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
                    is ResultWrapper.Error ->  {
                        if ("No address associated with hostname" in it.message) {
                            setState { copy(isLoading = false, error = "Отсутствует интернет") }
                        } else if ("No such company" in it.message) {
                            setState { copy(isLoading = false, error = "Компания отсутствует") }
                        } else if ("Пользователь с таким e-mail уже существует" in it.message) {
                            setState { copy(isLoading = false, error = "Почта занята") }
                        } else {
                            setState { copy(isLoading = false) }
                            setAction { RegisterAction.ShowError(it.message) }
                        }
                    }
                }
            }.collect()
        }
}

sealed class RegisterAction {
    data object NavigateToHomeScreen : RegisterAction()
    data class ShowError(val message: String) : RegisterAction()
}