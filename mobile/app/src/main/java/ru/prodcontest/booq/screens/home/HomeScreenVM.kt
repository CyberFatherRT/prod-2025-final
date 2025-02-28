package ru.prodcontest.booq.screens.home

import dagger.hilt.android.lifecycle.HiltViewModel
import ru.prodcontest.booq.util.BaseViewModel

@HiltViewModel
class HomeScreenVM : BaseViewModel<HomeScreenState, Nothing>() {
    override fun setInitialState() = HomeScreenState()
}