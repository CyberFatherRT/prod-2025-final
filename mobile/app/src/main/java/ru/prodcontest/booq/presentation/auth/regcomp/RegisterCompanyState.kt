package ru.prodcontest.booq.presentation.auth.regcomp

data class RegisterCompanyState(
    val isLoading: Boolean,
    val error: String = ""
)