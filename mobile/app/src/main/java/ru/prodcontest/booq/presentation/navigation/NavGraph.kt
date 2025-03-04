package ru.prodcontest.booq.presentation.navigation

//import ru.prodcontest.booq.presentation.auth.regcomp.RegisterCompanyScreen
//import ru.prodcontest.booq.presentation.auth.regcomp.RegisterCompanyScreenDestination
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.prodcontest.booq.presentation.auth.login.LoginScreen
import ru.prodcontest.booq.presentation.auth.login.LoginScreenDestination
import ru.prodcontest.booq.presentation.auth.regcomp.RegisterCompanyScreen
import ru.prodcontest.booq.presentation.auth.regcomp.RegisterCompanyScreenDestination
import ru.prodcontest.booq.presentation.auth.register.RegisterScreen
import ru.prodcontest.booq.presentation.auth.register.RegisterScreenDestination
import ru.prodcontest.booq.presentation.home.HomeScreen
import ru.prodcontest.booq.presentation.home.HomeScreenDestination
import ru.prodcontest.booq.presentation.map.MapScreen
import ru.prodcontest.booq.presentation.map.MapScreenDestination
import ru.prodcontest.booq.presentation.profile.ProfileScreen
import ru.prodcontest.booq.presentation.profile.ProfileScreenDestination
import ru.prodcontest.booq.presentation.qrScanner.QrScannerScreen
import ru.prodcontest.booq.presentation.qrScanner.QrScannerScreenDestination
import ru.prodcontest.booq.presentation.selectBuilding.SelectBuildingScreen
import ru.prodcontest.booq.presentation.selectBuilding.SelectBuildingScreenDestination
import ru.prodcontest.booq.presentation.verifications.VerificationsScreen
import ru.prodcontest.booq.presentation.verifications.VerificationsScreenDestination

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = HomeScreenDestination
    ) {
        composable<LoginScreenDestination> {
            LoginScreen(navController)
        }
        composable<RegisterScreenDestination> {
            RegisterScreen(navController)
        }
        composable<HomeScreenDestination> {
            HomeScreen(navController)
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
        composable<QrScannerScreenDestination> {
            QrScannerScreen(navController)
        }
        composable<SelectBuildingScreenDestination> {
            SelectBuildingScreen(navController)
        }
    }
}