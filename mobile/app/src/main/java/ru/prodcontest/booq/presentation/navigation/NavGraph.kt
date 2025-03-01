package ru.prodcontest.booq.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Auth.route) {
            Log.d("NavGraph", "ГОООООООООООЙДА")
        }

        composable(route = Screen.Home.route) {
            Log.d("NavGraph", "ГОООООООООООЙДА HOME")
        }

    }
}