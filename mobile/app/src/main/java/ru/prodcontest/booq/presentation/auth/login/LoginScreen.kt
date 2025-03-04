package ru.prodcontest.booq.presentation.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.presentation.auth.components.AuthTextData
import ru.prodcontest.booq.presentation.auth.login.components.LoginElement
import ru.prodcontest.booq.presentation.auth.regcomp.RegisterCompanyScreenDestination
import ru.prodcontest.booq.presentation.auth.register.RegisterScreenDestination
import ru.prodcontest.booq.presentation.home.HomeScreenDestination
import ru.prodcontest.booq.presentation.selectBuilding.SelectBuildingScreenDestination
import ru.prodcontest.booq.presentation.users.UsersScreenDestination
import ru.prodcontest.booq.presentation.verifications.VerificationsScreenDestination

@Serializable
object LoginScreenDestination

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val actionsScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        actionsScope.launch {
            viewModel.action.collect { action ->
                when(action) {
                    is LoginScreenAction.NavigateToHomeScreen -> {
                        navController.navigate(HomeScreenDestination.route)
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
    val companyDomainValid by remember { derivedStateOf { Regex("[a-zA-Z]{3,30}").matches(companyDomain)  || companyDomain.isEmpty() } }

    val isError = !(emailValid && passwordValid && companyDomainValid)

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val (boxText, boxLogin, createCompany) = createRefs()

            Text(
                text = "Войдите в аккаунт!",
                modifier = Modifier
                    .constrainAs(boxText) {
                        top.linkTo(parent.top, margin = 50.dp)
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
                    error = if (emailValid) "" else "Некорректный email"
                ),
                passwordData = AuthTextData(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Пароль",
                    error = if (!passwordValid) "Некорректный пароль" else ""
                ),
                companyDomainData = AuthTextData(
                    value = companyDomain,
                    onValueChange = { companyDomain = it },
                    placeholder = "Домен компании",
                    error = if (companyDomainValid) "" else "Длина домена от 3 до 30"
                ),
                isLoading = viewState.isLoading,
                isLocked = viewState.isLoading,
                isLockedLogin = isError or email.isEmpty() or password.isEmpty() or companyDomain.isEmpty(),
                onLoginClick = {
                    viewModel.login(
                        email = email,
                        password = password,
                        domain = companyDomain
                    )
                },
                onCreateAccountClick = { navController.navigate(RegisterScreenDestination) },
                error = viewState.error,
                modifier = Modifier
                    .padding(horizontal = 22.dp)
                    .constrainAs(boxLogin) {
                        top.linkTo(boxText.bottom, margin = 90.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .background(
                        color = Color(0xFF9EA2AA),
                        shape = RoundedCornerShape(
                            topEnd = 0.dp,
                            topStart = 0.dp,
                            bottomStart = 28.dp,
                            bottomEnd = 28.dp
                        )
                    )
                    .clip(
                        RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .clickable { navController.navigate(RegisterCompanyScreenDestination) }
                    .constrainAs(createCompany) {
                        start.linkTo(boxLogin.start, margin = 60.dp)
                        end.linkTo(boxLogin.end, margin = 60.dp)
                        top.linkTo(boxLogin.bottom)
                        width = Dimension.fillToConstraints
                    }
            ) {
                Text(
                    text = "Создать компанию",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF000000).copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color(0xFF000000).copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
            }

        }
    }
}