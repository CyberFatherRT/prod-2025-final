package ru.prodcontest.booq.presentation.users

import ru.prodcontest.booq.presentation.profile.ProfileScreenAction

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.domain.model.UserRole
import ru.prodcontest.booq.domain.model.VerificationModel
import ru.prodcontest.booq.domain.model.VerificationUserModel

@Serializable
object UsersScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(navController: NavController, vm: UsersScreenModel = hiltViewModel()) {
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
//            vm.action.collect { action ->
//                when (action) {
//                    is ProfileScreenAction.ShowError -> {
//                        snackbarHostState.showSnackbar(action.message)
//                    }
//                }
//            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Пользователи") },
                navigationIcon = {
                    IconButton({
                        navController.navigateUp()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
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

        state.usersInfo?.let { users ->
            val sigma =
                remember { mutableStateListOf<VerificationUserModel>(*users.toTypedArray()) }

            LazyColumn(Modifier.padding(innerPadding)) {

                items(items = sigma, key = { user ->
                    // Return a stable + unique key for the item
                    user.id
                }) { user ->
                    UserRow(user,
                       {
                            vm.deleteUser(user.id.toString())
                            sigma.remove(user)
                        })
                }
            }
        }

    }
}

@Composable
private fun UserRow(
    user: VerificationUserModel,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomButtonModifier =
        Modifier
            .padding(top = 6.dp, bottom = 6.dp, start = 6.dp, end = 6.dp)
            .size(30.dp)
    //.clip(CircleShape)

    Card(modifier = modifier) {
        Row {
            AsyncImage(
                model = user.avatarUrl
                    ?: "https://i.pinimg.com/736x/cb/45/72/cb4572f19ab7505d552206ed5dfb3739.jpg",
                contentDescription = null,
                modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
                    .padding(5.dp)
                    .wrapContentWidth(Alignment.Start)
            )

            Column {
                Text(
                    text = "${user.surname} ${user.name}",
                    modifier = Modifier,
                    fontSize = 15.sp
                    //.align(Alignment.CenterVertically)
                )

                Text(
                    text = user.email, fontSize = 10.sp
                )
            }
        }
        Row(Modifier.fillMaxWidth()) {
            Text(
                text = "Role: ${user.role}",
                fontSize = 10.sp
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onDecline,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = bottomButtonModifier,
                contentPadding = PaddingValues(0.dp)
            ) {

                Row(modifier = Modifier.align(Alignment.CenterVertically)) {
                    Icon(Icons.Default.Close, null)
                }
            }
        }
    }
}
