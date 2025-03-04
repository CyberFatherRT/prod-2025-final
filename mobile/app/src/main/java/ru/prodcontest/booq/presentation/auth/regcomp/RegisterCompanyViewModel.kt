package ru.prodcontest.booq.presentation.auth.regcomp

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.prodcontest.booq.data.remote.dto.RegisterCompanyDto
import ru.prodcontest.booq.domain.repository.ApiRepository
import ru.prodcontest.booq.domain.usecase.SetTokenUseCase
import ru.prodcontest.booq.domain.util.ResultWrapper
import ru.prodcontest.booq.presentation.BaseViewModel
import ru.prodcontest.booq.presentation.auth.login.LoginScreenAction

import javax.inject.Inject

@HiltViewModel
class RegisterCompanyViewModel @Inject constructor(
    private val apiRepository: ApiRepository,
    private val setTokenUseCase: SetTokenUseCase
): BaseViewModel<RegisterCompanyState, RegisterCompanyAction>() {
    override fun setInitialState() = RegisterCompanyState(
        isLoading = false
    )

    fun registerCompany(name: String, domain: String) =
        viewModelScope.launch {
            apiRepository.registerCompany(RegisterCompanyDto(domain = domain, name = name)).onEach {
                when (it) {
                    is ResultWrapper.Ok -> {
                        setTokenUseCase(it.data.token)
                        setAction { RegisterCompanyAction.NavigateToHomeScreen }
                    }

                    is ResultWrapper.Loading -> setState { copy(isLoading = true) }
                    is ResultWrapper.Error -> {
                        if ("No address associated with hostname" in it.message) {
                            setState { copy(isLoading = false, error = "Отсутствует интернет") }
                        } else if ("409" in it.message) {
                            setState { copy(isLoading = false, error = "Компания отсутствует") }
                        } else {
                            setState { copy(isLoading = false) }
                            setAction { RegisterCompanyAction.ShowError(it.message) }
                        }
                    }
                }
            }.collect()
        }
}

sealed class RegisterCompanyAction {
    data object NavigateToHomeScreen : RegisterCompanyAction()
    data class ShowError(val message: String) : RegisterCompanyAction()
}