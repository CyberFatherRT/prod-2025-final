package ru.prodcontest.booq.presentation.auth.login

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
        isLoading = false,
        error = null
    )

    fun login(email: String, password: String) = viewModelScope.launch {
        apiRepository.login(LoginDto(email, password)).onEach {
            when (it) {
                is ResultWrapper.Ok -> {
                    setTokenUseCase(it.data.token)
                    setAction { LoginScreenAction.NavigateToHomeScreen }
                }

                ResultWrapper.Loading -> setState { copy(isLoading = true) }
                is ResultWrapper.Error -> setState { copy(error = it.message) }
            }
        }.collect()
    }
}

sealed class LoginScreenAction {
    data object NavigateToHomeScreen : LoginScreenAction()
}