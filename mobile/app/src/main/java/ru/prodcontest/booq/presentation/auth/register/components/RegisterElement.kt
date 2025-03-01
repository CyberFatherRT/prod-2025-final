package ru.prodcontest.booq.presentation.auth.register.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.prodcontest.booq.presentation.auth.components.AuthTextData

@Composable
fun RegisterElement(
    emailData: AuthTextData,
    passwordData: AuthTextData,
    companyDomainData: AuthTextData,
    nameData: AuthTextData,
    isLoading: Boolean,
    isLocked: Boolean,
    onRegisterClick: () -> Unit,
    error: String = "",
    modifier: Modifier = Modifier
) {
}