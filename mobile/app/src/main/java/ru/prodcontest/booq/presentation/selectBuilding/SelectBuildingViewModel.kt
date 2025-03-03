package ru.prodcontest.booq.presentation.selectBuilding

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
class SelectBuildingViewModel @Inject constructor(
    private val apiRepository: ApiRepository
) : BaseViewModel<SelectBuildingScreenState, Nothing>() {
    override fun setInitialState() = SelectBuildingScreenState(
        loadingList = true,
        error = null,
        buildings = null
    )

    init {
        getBuildings()
    }

    fun getBuildings() = viewModelScope.launch {
        apiRepository.listPlaces().onEach {
            when(it) {
                is ResultWrapper.Ok -> {
                    setState { copy(loadingList = false, error = null, buildings = it.data) }
                }
                is ResultWrapper.Error -> {
                    setState { copy(loadingList = false, error = it.message, buildings = null) }
                }
                ResultWrapper.Loading -> {
                    setState { copy(loadingList = true, error = null, buildings = null) }
                }
            }
        }.collect()
    }
}