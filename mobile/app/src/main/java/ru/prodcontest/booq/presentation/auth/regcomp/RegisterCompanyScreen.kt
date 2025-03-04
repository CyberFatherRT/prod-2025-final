package ru.prodcontest.booq.presentation.auth.regcomp

import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.presentation.auth.components.AuthTextData
import ru.prodcontest.booq.presentation.auth.regcomp.components.RegisterCompanyElement
import ru.prodcontest.booq.presentation.home.HomeScreenDestination

@Serializable
object RegisterCompanyScreenDestination

@Composable
fun RegisterCompanyScreen(
    navController: NavController,
    viewModel: RegisterCompanyViewModel = hiltViewModel()
) {

    val viewState = viewModel.viewState.value
    val snackbarHostState = remember { SnackbarHostState() }

    val actionsScope = rememberCoroutineScope()

    var companyDomain by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    val nameValid by remember { derivedStateOf { (name.length > 1) and (name.length < 120) || name.isEmpty() } }
    val companyDomainValid by remember { derivedStateOf { Regex("[a-zA-Z]{3,30}").matches(companyDomain) || companyDomain.isEmpty() } }

    LaunchedEffect(Unit) {
        actionsScope.launch {
            viewModel.action.collect { action ->
                when(action) {
                    is RegisterCompanyAction.NavigateToHomeScreen -> {
                        val route = HomeScreenDestination.routeWithArgs.replace(
                            "{${HomeScreenDestination.companyNameArg}}",
                            companyDomain
                        )
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                    is RegisterCompanyAction.ShowError -> {
                        snackbarHostState.showSnackbar(action.message)
                    }
                }
            }
        }
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val (boxText, boxLogin) = createRefs()

        Text(
            text = "Создание компании",
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

        RegisterCompanyElement(
            nameData = AuthTextData(
                value = name,
                onValueChange = { name = it },
                placeholder = "Имя",
                error = if (nameValid) "" else "Некорректное имя"
            ),
            companyDomainData = AuthTextData(
                value = companyDomain,
                onValueChange = { companyDomain = it },
                placeholder = "Домен компании",
                error = if (companyDomainValid) "" else "Некорректный домен компании"

            ),
            isLoading = viewState.isLoading,
            isLocked = !companyDomainValid or !nameValid or name.isEmpty() or companyDomain.isEmpty(),
            onRegisterCompanyClick = { viewModel.registerCompany(name, companyDomain) },
            error = viewState.error,
            onBackClick = { navController.navigateUp() },
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