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
import ru.prodcontest.booq.presentation.auth.components.AuthTextData
import ru.prodcontest.booq.presentation.auth.login.LoginScreenDestination
import ru.prodcontest.booq.presentation.auth.register.components.RegisterElement
import ru.prodcontest.booq.presentation.profile.ProfileScreenDestination
import ru.prodcontest.booq.presentation.theme.BooqTheme

@Serializable
object RegisterScreenDestination

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewState.value

    val actionsScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        actionsScope.launch {
            viewModel.action.collect { action ->
                when(action) {
                    is RegisterScreenAction.NavigateToHomeScreen -> {
                        navController.navigate(ProfileScreenDestination)
                    }
                }
            }
        }
    }

    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var companyDomain by remember { mutableStateOf("") }

    val emailValid by remember { derivedStateOf { Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").matches(email) || email.isEmpty() } }
    val passwordValid by remember { derivedStateOf { Regex("[a-zA-Z0-9\$&+,:;=?@#|'<>.^*()%!-]{8,}").matches(password)  || password.isEmpty() } }
    val nameValid by remember { derivedStateOf { (name.length > 1) and (name.length < 120) || name.isEmpty() } }
    val surnameValid by remember { derivedStateOf { (surname.length > 1) and (surname.length < 120) || surname.isEmpty() } }
    val companyDomainValid by remember { derivedStateOf { Regex("[a-zA-Z]{3,30}").matches(companyDomain) || companyDomain.isEmpty() } }

    val isError = !(emailValid && passwordValid && nameValid && surnameValid && companyDomainValid)

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

//        Box(
//            modifier = Modifier
//                .height(300.dp)
//                .width(80.dp)
//                .offset(x = -40.dp)
//                .background(
//                    color = MaterialTheme.colorScheme.primary,
//                    shape = RoundedCornerShape(41.dp),
//                )
//                .constrainAs(boxBackground) {
//                    top.linkTo(boxLogin.top, margin = 80.dp)
//                    start.linkTo(boxLogin.end)
//                }
//        )

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
                error = if (companyDomainValid) "" else "Некорректный домен компании"

            ),
            isLoading = viewState.isLoading,
            isLocked = false,
            isLockedRegister = isError or name.isEmpty() or surname.isEmpty() or email.isEmpty() or password.isEmpty() or companyDomain.isEmpty(),
            onRegisterClick = { viewModel.register(name, surname, email, password, companyDomain) },
            onLoginClick = { navController.navigate(LoginScreenDestination) },
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