package ru.prodcontest.booq.presentation.selectBuilding

import ru.prodcontest.booq.data.remote.dto.PlaceDto

data class SelectBuildingScreenState(
    val loadingList: Boolean,
    val buildings: List<PlaceDto>?,
    val error: String?
)
