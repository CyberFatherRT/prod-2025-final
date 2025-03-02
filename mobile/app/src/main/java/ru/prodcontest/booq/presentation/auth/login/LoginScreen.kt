package ru.prodcontest.booq.presentation.auth.login

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.presentation.auth.components.AuthButton
import ru.prodcontest.booq.presentation.auth.components.AuthTextData
import ru.prodcontest.booq.presentation.auth.login.components.LoginElement
import ru.prodcontest.booq.presentation.profile.ProfileScreenDestination
import ru.prodcontest.booq.presentation.theme.BooqTheme

@Serializable
object LoginScreenDestination

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val actionsScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        actionsScope.launch {
            viewModel.action.collect { action ->
                when(action) {
                    is LoginScreenAction.NavigateToHomeScreen -> {
                        navController.navigate(ProfileScreenDestination)
                    }
                    is LoginScreenAction.ShowError -> {
                        snackbarHostState.showSnackbar(action.message)
                    }
                }
            }
        }
    }

    val viewState = viewModel.viewState.value
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val emailValid by remember { derivedStateOf { Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").matches(email) } }
    val passwordValid by remember { derivedStateOf { Regex("[a-zA-Z0-9\$&+,:;=?@#|'<>.^*()%!-]{8,}").matches(password) } }


    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val (boxText, boxLogin) = createRefs()

        Text(
            text = "Войдите в аккаунт!",
            modifier = Modifier
                .constrainAs(boxText) {
                    top.linkTo(parent.top, margin = 130.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        )

        LoginElement(
            emailData = AuthTextData(
                value = email,
                onValueChange = { email = it },
                placeholder = "Почта",
                error = if (emailValid) "Гойда почта" else ""
            ),
            passwordData = AuthTextData(
                value = password,
                onValueChange = { password = it },
                placeholder = "Пароль",
                error = if (passwordValid) "Гойда пароль" else ""
            ),
            isLoading = viewState.isLoading,
            isLocked = false,
            onLoginClick = { },
            onCreateAccountClick = { },
            error = "Нижний лейбл",
            modifier = Modifier
                .padding(horizontal = 22.dp)
                .constrainAs(boxLogin) {
                    top.linkTo(boxText.bottom, margin = 100.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2A2E37)
@Composable
fun DemoLoginScreen() {
    BooqTheme {

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isClick by remember { mutableStateOf(false) }

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val (boxText, boxLogin, boxEl) = createRefs()

            Spacer(modifier = Modifier.height(24.dp))

            AuthButton(
                text = "Войти",
                onClick = { },
                isLoaded = false,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}