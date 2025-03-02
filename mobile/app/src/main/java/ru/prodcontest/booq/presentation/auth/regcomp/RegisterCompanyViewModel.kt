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

import javax.inject.Inject

@HiltViewModel
class RegisterCompanyViewModel @Inject constructor(
    private val apiRepository: ApiRepository,
    private val setTokenUseCase: SetTokenUseCase
): BaseViewModel<RegisterCompanyState, RegisterCompanyAction>() {
    override fun setInitialState() = RegisterCompanyState(
        isLoading = false,
        error = null
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
                    is ResultWrapper.Error -> setState { copy(error = it.message) }
                }
            }.collect()
        }
}

sealed class RegisterCompanyAction {
    data object NavigateToHomeScreen : RegisterCompanyAction()
}