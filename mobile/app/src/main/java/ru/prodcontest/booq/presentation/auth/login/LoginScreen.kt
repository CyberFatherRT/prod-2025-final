package ru.prodcontest.booq.presentation.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import ru.prodcontest.booq.presentation.auth.components.AuthTextData
import ru.prodcontest.booq.presentation.auth.components.AuthTextField
import ru.prodcontest.booq.presentation.auth.login.components.LoginElement
import ru.prodcontest.booq.presentation.theme.BooqTheme

@Serializable
object LoginScreenDestination

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel()
) {

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
                    .offset(x = -50.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .height(200.dp)
                    .width(80.dp)
                    .constrainAs(boxEl) {
                        top.linkTo(boxLogin.top, margin = 65.dp)
                    }
            )

            LoginElement(
                emailData = AuthTextData(
                    value = email,
                    placeholder = "Почта",
                    onValueChange = { email = it }
                ),
                passwordData = AuthTextData(
                    value = password,
                    placeholder = "Пароль",
                    onValueChange = { password = it }
                ),
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
