package ru.prodcontest.booq.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.prodcontest.booq.presentation.auth.login.LoginScreen
import ru.prodcontest.booq.presentation.auth.login.LoginScreenDestination
import ru.prodcontest.booq.presentation.auth.regcomp.RegisterCompanyScreen
import ru.prodcontest.booq.presentation.auth.regcomp.RegisterCompanyScreenDestination
//import ru.prodcontest.booq.presentation.auth.regcomp.RegisterCompanyScreen
//import ru.prodcontest.booq.presentation.auth.regcomp.RegisterCompanyScreenDestination
import ru.prodcontest.booq.presentation.auth.register.RegisterScreen
import ru.prodcontest.booq.presentation.auth.register.RegisterScreenDestination
import ru.prodcontest.booq.presentation.map.MapScreen
import ru.prodcontest.booq.presentation.map.MapScreenDestination
import ru.prodcontest.booq.presentation.profile.ProfileScreen
import ru.prodcontest.booq.presentation.profile.ProfileScreenDestination
import ru.prodcontest.booq.presentation.verifications.VerificationsScreenDestination
import ru.prodcontest.booq.presentation.verifications.VerificationsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = LoginScreenDestination
    ) {
        composable<LoginScreenDestination> {
//            LoginScreen(navController)
            MapScreen()
        }
        composable<RegisterScreenDestination> {
            RegisterScreen(navController)
        }
        composable<RegisterCompanyScreenDestination> {
            RegisterCompanyScreen(navController)
        }
        composable<ProfileScreenDestination> {
            ProfileScreen(navController)
        }
        composable<MapScreenDestination> {
            MapScreen()
        }
        composable<VerificationsScreenDestination> {
            VerificationsScreen(navController)
        }
    }
}