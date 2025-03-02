package ru.prodcontest.booq.presentation.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
    var companyDomain by remember { mutableStateOf("") }

    val emailValid by remember { derivedStateOf { Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").matches(email) || email.isEmpty() } }
    val passwordValid by remember { derivedStateOf { Regex("[a-zA-Z0-9\$&+,:;=?@#|'<>.^*()%!-]{8,}").matches(password)  || password.isEmpty() } }
    val companyDomainValid by remember { derivedStateOf { Regex("[a-zA-Z0-9\$&+,:;=?@#|'<>.^*()%!-]{8,}").matches(password)  || password.isEmpty() } }

    val isLocked by remember { mutableStateOf("") }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val (boxText, boxLogin, boxBackground) = createRefs()

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

        Box(
            modifier = Modifier
                .height(260.dp)
                .width(80.dp)
                .offset(x = (-50).dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(41.dp),
                )
                .constrainAs(boxBackground) {
                    top.linkTo(boxLogin.top, margin = 60.dp)
                    start.linkTo(boxLogin.start)
                }
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
            companyDomainData = AuthTextData(
                value = companyDomain,
                onValueChange = { companyDomain = it },
                placeholder = "Домен компании",
                error = if (companyDomainValid) "Гойд домен" else ""
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