package ru.prodcontest.booq.presentation.navigation

sealed class Screen(val route: String) {
    data object Auth : Screen("auth_screen")
    data object Home : Screen("home_screen")
    // Сюды добавляем новые скрины.
}