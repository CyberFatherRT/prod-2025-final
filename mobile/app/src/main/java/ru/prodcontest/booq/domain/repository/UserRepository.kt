package ru.prodcontest.booq.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.prodcontest.booq.domain.model.UserData

interface UserRepository {

    fun getFlowUserData(): StateFlow<UserData>

    fun getUserData(): UserData

    fun isAuthorized(): Boolean
}
