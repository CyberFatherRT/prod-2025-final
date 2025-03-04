package ru.prodcontest.booq.presentation.auth.login

data class LoginScreenState(
    val isLoading: Boolean,
    val error: String = ""
)
