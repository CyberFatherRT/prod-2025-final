package ru.prodcontest.booq.presentation.auth.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable
import ru.prodcontest.booq.R
import ru.prodcontest.booq.presentation.auth.components.AuthButton
import ru.prodcontest.booq.presentation.auth.components.AuthTextField

@Serializable
object LoginScreenDestination

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel()
) {
//    val viewState by viewModel.viewState.collectAsState()

//    val viewState = viewModel.viewState.value
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//
//    val emailValid by remember { derivedStateOf { Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").matches(email) } }
//    val passwordValid by remember { derivedStateOf { Regex("[a-zA-Z0-9\$&+,:;=?@#|'<>.^*()%!-]{8,}").matches(password) } }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            AuthTextField(
//                value = email,
//                onValueChange = { email = it },
//                placeholder = "Email",
//                isLocked = viewState.isLoading,
//                iconResId = R.drawable.mail_24
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            AuthTextField(
//                value = password,
//                onValueChange = { password = it },
//                placeholder = "Password",
//                isPassword = true,
//                isLocked = viewState.isLoading,
//                iconResId = R.drawable.key_24
//            )
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            AuthButton(
//                text = "Войти",
//                onClick = { viewModel.login(email, password) },
//                isLoaded = viewState.isLoading,
//                modifier = Modifier.padding(top = 16.dp)
//            )
//        }
//    }
}