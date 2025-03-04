package ru.prodcontest.booq.presentation.auth.login

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.prodcontest.booq.data.remote.dto.LoginDto
import ru.prodcontest.booq.domain.repository.ApiRepository
import ru.prodcontest.booq.domain.usecase.SetTokenUseCase
import ru.prodcontest.booq.domain.util.ResultWrapper
import ru.prodcontest.booq.presentation.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiRepository: ApiRepository,
    private val setTokenUseCase: SetTokenUseCase
) : BaseViewModel<LoginScreenState, LoginScreenAction>() {

    override fun setInitialState() = LoginScreenState(
        isLoading = false
    )

    fun login(email: String, password: String, domain: String) = viewModelScope.launch {
        apiRepository.login(LoginDto(email, password, domain)).onEach {
            when (it) {
                is ResultWrapper.Ok -> {
                    setState { copy(isLoading = false) }
                    runBlocking { setTokenUseCase(it.data.token) }
                    setAction { LoginScreenAction.NavigateToHomeScreen }
                }

                ResultWrapper.Loading -> setState { copy(isLoading = true) }
                is ResultWrapper.Error ->  {

                    if ("No address associated with hostname" in it.message) {
                        setState { copy(isLoading = false, error = "Отсутствует интернет") }
                    } else if ("No such user" in it.message) {
                        setState { copy(isLoading = false, error = "Некорректные данные") }
                    } else if ("wrong password" in it.message) {
                        setState { copy(isLoading = false, error = "Неверный пароль") }
                    } else {
                        setState { copy(isLoading = false) }
                        setAction { LoginScreenAction.ShowError(it.message) }
                    }

                    Log.d("MEOW", it.message)
                }
            }
        }.collect()
    }
}


sealed class LoginScreenAction {
    data object NavigateToHomeScreen : LoginScreenAction()
    data class ShowError(val message: String) : LoginScreenAction()
}