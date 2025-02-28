package ru.prodcontest.booq.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable

@Serializable
object HomeScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: HomeScreenVM = viewModel()) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = {
                Text("Мои записи")
            }, actions = {
                IconButton({}) { Icon(Icons.Default.AccountCircle, null) }
            }
        )
    }) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            Text("hue")
        }
    }
}