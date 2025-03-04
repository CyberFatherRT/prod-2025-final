package ru.prodcontest.booq.presentation.map

import ru.prodcontest.booq.data.remote.dto.CoworkingDto
import ru.prodcontest.booq.domain.model.CoworkingItemModel

data class MapScreenState(
    val isLoadingCoworkings: Boolean,
    val coworkings: List<CoworkingDto>?,
    val selectedCoworking: CoworkingDto?,

    val coworkingData: List<CoworkingItemModel>?,
    val isCoworkingDataLoading: Boolean,

    val selectedItem: CoworkingItemModel?,
    val bottomSheetOpened: Boolean,

    val isCreatingBooking: Boolean,
)
