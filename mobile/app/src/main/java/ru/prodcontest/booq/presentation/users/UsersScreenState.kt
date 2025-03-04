package ru.prodcontest.booq.presentation.users

import ru.prodcontest.booq.domain.model.VerificationUserModel

data class UsersScreenState(
    var usersInfo: List<VerificationUserModel>?,
    val isLoading: Boolean,
    val error: String?
)
