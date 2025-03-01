package ru.prodcontest.booq.presentation.auth.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.R
import ru.prodcontest.booq.presentation.auth.components.AuthButton
import ru.prodcontest.booq.presentation.auth.components.AuthTextField
import ru.prodcontest.booq.presentation.auth.login.components.LoginElement
import ru.prodcontest.booq.presentation.theme.BooqTheme

@Serializable
object LoginScreenDestination

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel()
) {
//    val viewState by viewModel.viewState.collectAsState()

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
            email = email,
            onEmailChange = { email = it },
            mailError = if (emailValid) "Гойда почта" else "", // ИЗМЕНИТЬ
            password = password,
            passwordError = if (passwordValid) "Гойда пароль" else "", // ИЗМЕНИТЬ
            onPasswordChange = { password = it },
            isLoading = viewState.isLoading,
            isLocked = false, // ИЗМЕНИТЬ
            onLoginClick = { }, // ЭВЕНТ НА НАЖАТИЕ СВОЙ
            onCreateAccountClick = { }, // ЭВЕНТ НА НАЖАТИЕ СВОЙ
            error = "Нижний лейбл", // ЭТО НИЖНИЙ ЛЕЙБЛ С ОШИБКОЙ
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
fun DemoScreen() {
    BooqTheme {

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isClick by remember { mutableStateOf(false) }

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
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                isLoading = isClick,
                isLocked = false,
                error = "Привет как дела",
                onLoginClick = { isClick = true },
                onCreateAccountClick = { },
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
}
