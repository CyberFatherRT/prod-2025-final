package ru.prodcontest.booq.presentation.profile

import ru.prodcontest.booq.domain.model.UserModel

data class ProfileScreenState(
    val profileInfo: UserModel?,
    val isLoading: Boolean,
    val error: String?,

    val documentLoadingProgress: Float?,
    val documentUploaded: Boolean
)
