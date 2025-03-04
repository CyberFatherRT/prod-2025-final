package ru.prodcontest.booq.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.domain.model.UserRole
import ru.prodcontest.booq.presentation.auth.login.LoginScreenDestination

@Serializable
object ProfileScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, vm: ProfileViewModel = hiltViewModel()) {
    val state = vm.viewState.value
//    val state = ProfileScreenState(
//        isLoading = false, error = null, profileInfo = UserModel(
//            name = "Ivan",
//            surname = "Kuznetsov",
//            avatarUrl = "https://resources.jetbrains.com/storage/products/company/brand/logos/Ktor_icon.png",
//            role = UserRole.Guest,
//            companyId = UUID.randomUUID(),
//            email = "meow@nya.ru",
//            pendingVerification = false
//        )
//    )
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val actionsScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        actionsScope.launch {
            vm.action.collect { action ->
                when(action) {
                    is ProfileScreenAction.ShowError -> {
                        snackbarHostState.showSnackbar(action.message)
                    }

                    ProfileScreenAction.NavigateToLoginScreen -> {
                        navController.navigate(LoginScreenDestination) {
                            popUpTo(0)
                        }
                    }
                }
            }
        }
    }

    val fileChooserLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            if (it == null) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message = "Файл не выбран")
                }
            } else {
                vm.uploadFile(it)
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton({
                        navController.navigateUp()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton({
                        vm.exit()
                    }) { Icon(Icons.Default.ExitToApp, null) }
                },
                scrollBehavior = null
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            }
        }
        state.profileInfo?.let { profile ->
            Column(Modifier.padding(innerPadding)) {
                Row(Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    SubcomposeAsyncImage(
                        model = profile.avatarUrl ?: "https://camo.githubusercontent.com/c77fbaaf11748827d9df62416bb949748e9c5427dce32c88d09d4d689f7e8c08/68747470733a2f2f612e736c61636b2d656467652e636f6d2f64663130642f696d672f617661746172732f6176615f303032322d3531322e706e67",
                        contentDescription = null,
                        loading = {
                            CircularProgressIndicator(Modifier.size(32.dp))
                        },
                        modifier = Modifier.size(96.dp).padding(12.dp).clip(RoundedCornerShape(48.dp))
                    )
                    Column(Modifier.padding(8.dp)) {
                        Text(
                            "${profile.name} ${profile.surname}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(profile.email)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.padding(16.dp))
                    Text(
                        "Ваша роль: ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)
                    )
                    Text(
                        when (profile.role) {
                            UserRole.Admin -> "Администратор"
                            UserRole.Student -> "Студент"
                            UserRole.Guest -> "Гость"
                            UserRole.VerifiedGuest -> "Подтверждённый гость"
                        }, style = MaterialTheme.typography.titleMedium
                    )
                }
                if (profile.role == UserRole.Guest) {
                    if (profile.pendingVerification) {
                        Card(
                            Modifier.padding(6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Row(
                                Modifier.padding(4.dp, 12.dp, 4.dp, 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Build, null, Modifier.padding(8.dp, 0.dp))
                                Text(
                                    "Ожидается подтверждение",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Text(
                                "Администратор проверит Ваши документы и подтвердит аккаунт. Вам придёт уведомление, когда аккаунт будет подтверждён.",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(7.dp, 4.dp, 8.dp, 10.dp)
                            )
                        }
                    } else {
                        Card(
                            Modifier.padding(6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Row(
                                Modifier.padding(4.dp, 12.dp, 4.dp, 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, null, Modifier.padding(8.dp, 0.dp))
                                Text(
                                    "Загрузите документы",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Text(
                                "Необходимо загрузить документ, подтверждающий личность, чтобы получить доступ к бронированию.",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(8.dp, 4.dp)
                            )
                            if (!state.documentUploaded && state.documentLoadingProgress == null) {
                                Button(
                                    {
                                        fileChooserLauncher.launch(arrayOf("application/pdf"))
                                    },
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp, 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowUp,
                                        null,
                                        Modifier.padding(4.dp, 0.dp)
                                    )
                                    Text("Прикрепить файл")
                                }
                            }
                            if (state.documentLoadingProgress != null) {
                                LinearProgressIndicator(
//                                    progress = { state.documentLoadingProgress },
                                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                                )
                            }
                            if (state.documentUploaded) {
                                Row(
                                    Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, null, Modifier.padding(8.dp, 4.dp))
                                    Text("Успешно загружено")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview
@Composable
private fun ProfileScreenPreview1() {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(Color.Red.toArgb())
    }

    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        ProfileScreen(rememberNavController())
    }
}