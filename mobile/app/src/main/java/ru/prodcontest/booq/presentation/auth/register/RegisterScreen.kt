package ru.prodcontest.booq.presentation.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.presentation.auth.components.AuthTextData
import ru.prodcontest.booq.presentation.auth.login.LoginViewModel
import ru.prodcontest.booq.presentation.auth.login.components.LoginElement
import ru.prodcontest.booq.presentation.auth.register.components.RegisterElement
import ru.prodcontest.booq.presentation.theme.BooqTheme

@Serializable
object RegisterScreenDestination

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {

    val viewState = viewModel.viewState.value

    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var companyDomain by remember { mutableStateOf("") }

    val emailValid by remember { derivedStateOf { Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").matches(email) || email.isEmpty() } }
    val passwordValid by remember { derivedStateOf { Regex("[a-zA-Z0-9\$&+,:;=?@#|'<>.^*()%!-]{8,}").matches(password)  || password.isEmpty() } }
    val nameValid = true
    val surnameValid = true
    val companyDomainValid = true

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val (boxText, boxLogin, boxBackground) = createRefs()

        Text(
            text = "Создание аккаунта!",
            modifier = Modifier
                .constrainAs(boxText) {
                    top.linkTo(parent.top, margin = 70.dp)
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
                .height(300.dp)
                .width(80.dp)
                .offset(x = -40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(41.dp),
                )
                .constrainAs(boxBackground) {
                    top.linkTo(boxLogin.top, margin = 80.dp)
                    start.linkTo(boxLogin.end)
                }
        )

        RegisterElement(
            nameData = AuthTextData(
                value = name,
                onValueChange = { name = it },
                placeholder = "Имя",
                error = if (nameValid) "" else "Некорректное имя"
            ),
            surnameData = AuthTextData(
                value = surname,
                onValueChange = { surname = it },
                placeholder = "Фамилия",
                error = if (surnameValid) "" else "Некорректная фамилия"
            ),
            emailData = AuthTextData(
                value = email,
                onValueChange = { email = it },
                placeholder = "Почта",
                error = if (emailValid) "" else "Некорректный email"
            ),
            passwordData = AuthTextData(
                value = password,
                onValueChange = { password = it },
                placeholder = "Пароль",
                error = if (passwordValid) "" else "Некорректный пароль"
            ),
            companyDomainData = AuthTextData(
                value = companyDomain,
                onValueChange = { companyDomain = it },
                placeholder = "Домен компании",
                error = if (passwordValid) "" else "Некорректный домен компании"
            ),
            isLoading = viewState.isLoading,
            isLocked = false,
            onRegisterClick = { },
            onLoginClick = { },
            error = "",
            modifier = Modifier
                .padding(horizontal = 22.dp)
                .constrainAs(boxLogin) {
                    top.linkTo(boxText.bottom, margin = 40.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2A2E37)
@Composable
fun DemoRegisterScreen() {

    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var companyDomain by remember { mutableStateOf("") }



    BooqTheme {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val (boxText, boxLogin) = createRefs()

            Text(
                text = "Создание аккаунта!",
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



        }
    }
}