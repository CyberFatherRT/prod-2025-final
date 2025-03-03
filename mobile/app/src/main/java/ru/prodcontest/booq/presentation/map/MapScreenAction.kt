package ru.prodcontest.booq.presentation.map

sealed class MapScreenAction {
    data class ShowLoadingCoworkingsError(val message: String) : MapScreenAction()
    data class ShowLoadingCoworkingDataError(val message: String) : MapScreenAction()
}