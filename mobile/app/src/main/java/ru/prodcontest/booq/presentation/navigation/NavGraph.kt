package ru.prodcontest.booq.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.prodcontest.booq.presentation.auth.login.LoginScreen
import ru.prodcontest.booq.presentation.auth.login.LoginScreenDestination
import ru.prodcontest.booq.presentation.auth.register.RegisterScreen
import ru.prodcontest.booq.presentation.auth.register.RegisterScreenDestination

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = LoginScreenDestination
    ) {
        composable<LoginScreenDestination> {
            LoginScreen()
        }
        composable<RegisterScreenDestination> {
            RegisterScreen()
        }
    }
}