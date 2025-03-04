package ru.prodcontest.booq.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<UiState, Action> : ViewModel() {
    abstract fun setInitialState(): UiState

    private val initialState: UiState by lazy { setInitialState() }

    private val _viewState: MutableState<UiState> = mutableStateOf(initialState)
    val viewState: State<UiState> = _viewState

    private val _action = Channel<Action>()
    val action = _action.receiveAsFlow()

    fun setState(reducer: UiState.() -> UiState) {
        val newState = viewState.value.reducer()
        _viewState.value = newState
    }

    protected fun setAction(builder: () -> Action) {
        val actionValue = builder()
        viewModelScope.launch { _action.send(actionValue) }
    }
}