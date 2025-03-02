package ru.prodcontest.booq.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.prodcontest.booq.presentation.auth.login.LoginScreen
import ru.prodcontest.booq.presentation.auth.login.LoginScreenDestination
import ru.prodcontest.booq.presentation.auth.register.RegisterScreen
import ru.prodcontest.booq.presentation.auth.register.RegisterScreenDestination
import ru.prodcontest.booq.presentation.profile.ProfileScreen
import ru.prodcontest.booq.presentation.profile.ProfileScreenDestination

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = LoginScreenDestination
    ) {
        composable<LoginScreenDestination> {
            LoginScreen(navController)
        }
        composable<RegisterScreenDestination> {
            RegisterScreen()
        }
        composable<ProfileScreenDestination> {
            ProfileScreen(navController)
        }
    }
}