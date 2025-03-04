package ru.prodcontest.booq.presentation.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.prodcontest.booq.data.remote.dto.CoworkingDto
import ru.prodcontest.booq.data.remote.dto.CreateBookingDto
import ru.prodcontest.booq.data.remote.dto.PatchBookingDto
import ru.prodcontest.booq.domain.model.CoworkingItemModel
import ru.prodcontest.booq.domain.repository.ApiRepository
import ru.prodcontest.booq.domain.usecase.GetCoworkingDataUseCase
import ru.prodcontest.booq.domain.util.ResultWrapper
import ru.prodcontest.booq.presentation.BaseViewModel
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val apiRepository: ApiRepository,
    private val getCoworkingDataUseCase: GetCoworkingDataUseCase
) : BaseViewModel<MapScreenState, MapScreenAction>() {
    override fun setInitialState() = MapScreenState(
        isLoadingCoworkings = true,
        coworkings = null,
        selectedCoworking = null,
        coworkingData = null,
        isCoworkingDataLoading = false,
        selectedItem = null,
        bottomSheetOpened = false,
        isCreatingBooking = false
    )

    val navArgs: MapScreenDestination
        get() = savedStateHandle.toRoute()

    init {
        if (navArgs.bookingId != null) {
            viewModelScope.launch {
                apiRepository.listCoworkings().onEach { coworkings ->
                    when (coworkings) {
                        is ResultWrapper.Ok -> {
                            val myCowo =
                                coworkings.data.first { it.id == navArgs.coworkingsSpaceId!! }
                            loadCoworkings(myCowo.buildingId)
                        }

                        is ResultWrapper.Error -> {
                            setState { copy(isLoadingCoworkings = false, coworkings = null) }
                            setAction { MapScreenAction.ShowLoadingCoworkingsError(coworkings.message) }
                        }

                        ResultWrapper.Loading -> {
                            setState { copy(isLoadingCoworkings = true, coworkings = null) }
                        }
                    }
                }.collect()
            }
        } else {
            if (navArgs.buildingId != null) {
                loadCoworkings()
            }
        }
    }

    fun loadCoworkings(buildingId: String = navArgs.buildingId!!) = viewModelScope.launch {
        apiRepository.getCoworkingsOfBuilding(buildingId).onEach {
            when (it) {
                is ResultWrapper.Ok -> {
                    setState {
                        copy(
                            isLoadingCoworkings = false,
                            coworkings = it.data,
                            selectedCoworking = it.data.first()
                        )
                    }
                    updateCoworkingData()
                }

                is ResultWrapper.Error -> {
                    setState { copy(isLoadingCoworkings = false, coworkings = null) }
                    setAction { MapScreenAction.ShowLoadingCoworkingsError(it.message) }
                }

                ResultWrapper.Loading -> {
                    setState { copy(isLoadingCoworkings = true, coworkings = null) }
                }
            }
        }.collect()
    }

    fun selectCoworking(coworking: CoworkingDto) = viewModelScope.launch {
        setState { copy(selectedCoworking = coworking, bottomSheetOpened = false) }
        updateCoworkingData().join()
    }

    private fun updateCoworkingData() = viewModelScope.launch {
        viewState.value.selectedCoworking?.let { selectedCoworking ->
            getCoworkingDataUseCase(
                selectedCoworking.buildingId,
                coworkingId = selectedCoworking.id,
                height = selectedCoworking.height
            ).onEach {
                when (it) {
                    is ResultWrapper.Ok -> {
                        setState { copy(coworkingData = it.data, isCoworkingDataLoading = false) }
                    }

                    is ResultWrapper.Error -> {
                        setState { copy(coworkingData = null, isCoworkingDataLoading = false) }
                        setAction { MapScreenAction.ShowLoadingCoworkingDataError(it.message) }
                    }

                    ResultWrapper.Loading -> {
                        setState { copy(coworkingData = null, isCoworkingDataLoading = true) }
                    }
                }
            }.collect()
        }
    }

    fun selectItem(item: CoworkingItemModel) {
        setState { copy(bottomSheetOpened = true, selectedItem = item) }
    }

    fun closeBottomSheet() = setState { copy(bottomSheetOpened = false) }

    fun confirmBooking(item: CoworkingItemModel, startLDT: LocalDateTime, endLDT: LocalDateTime) =
        viewModelScope.launch {
            if (navArgs.bookingId == null) {
                apiRepository.createBooking(
                    CreateBookingDto(
                        coworkingId = viewState.value.selectedCoworking!!.id,
                        coworkingItemId = item.id,
                        timeStart = startLDT.toString(),
                        timeEnd = endLDT.toString()
                    )
                ).onEach {
                    when (it) {
                        is ResultWrapper.Ok -> {
                            setState { copy(isCreatingBooking = false) }
                            setAction { MapScreenAction.EndBooking }
                        }

                        is ResultWrapper.Error -> {
                            setState { copy(isCreatingBooking = false) }
                            setAction { MapScreenAction.ShowBookingCreationError(it.message) }
                        }

                        ResultWrapper.Loading -> {
                            setState { copy(isCreatingBooking = true) }
                        }
                    }
                }.collect()
            } else {
                apiRepository.updateBooking(
                    navArgs.bookingId!!, PatchBookingDto(
                        coworkingId = viewState.value.selectedCoworking!!.id,
                        coworkingItemId = item.id,
                        timeStart = startLDT.toString(),
                        timeEnd = endLDT.toString()
                    )
                ).onEach {
                    when (it) {
                        is ResultWrapper.Ok -> {
                            setState { copy(isCreatingBooking = false) }
                            setAction { MapScreenAction.EndBooking }
                        }

                        is ResultWrapper.Error -> {
                            setState { copy(isCreatingBooking = false) }
                            setAction { MapScreenAction.ShowBookingCreationError(it.message) }
                        }

                        ResultWrapper.Loading -> {
                            setState { copy(isCreatingBooking = true) }
                        }
                    }
                }.collect()
            }
        }
}