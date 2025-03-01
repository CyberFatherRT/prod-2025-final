package ru.prodcontest.booq.presentation.auth.login.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ru.prodcontest.booq.R
import ru.prodcontest.booq.presentation.auth.components.AuthButton
import ru.prodcontest.booq.presentation.auth.components.AuthTextField
import ru.prodcontest.booq.presentation.theme.BooqTheme

@Composable
fun LoginElement(
    email: String,
    onEmailChange: (String) -> Unit,
    mailError: String = "",
    password: String,
    passwordError: String = "",
    onPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    isLocked: Boolean,
    onLoginClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    error: String = "",
    modifier: Modifier = Modifier,
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(
                RoundedCornerShape(32.dp)
            )
            .background(MaterialTheme.colorScheme.secondary)

    ) {

        val (textTopLabel, generalBox) = createRefs()

        Text(
            text = "Войти",
            modifier = Modifier
                .constrainAs(textTopLabel) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top, margin = 11.dp)
                },
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 20.sp
            ),
            color = Color.Gray
        )

        ConstraintLayout(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = RoundedCornerShape(32.dp)
                )
                .constrainAs(generalBox) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(textTopLabel.bottom, margin = 13.dp)
                }
        ) {

            val horizontalMargin = 8.dp

            val (emailBox, passwordBox, buttonBox, errorMsg, createAccount) = createRefs()

            Box(
                modifier = Modifier
                    .constrainAs(emailBox) {
                        start.linkTo(parent.start, margin = horizontalMargin)
                        end.linkTo(parent.end, margin = horizontalMargin)
                        top.linkTo(parent.top, margin = 22.dp)
                        width = Dimension.fillToConstraints
                    }
            ) {
                AuthTextField(
                    value = email,
                    error = mailError,
                    onValueChange = onEmailChange,
                    placeholder = "Почта",
                    isLocked = isLoading,
                    iconResId = R.drawable.mail_24,
                )
            }

            Box(
                modifier = Modifier
                    .constrainAs(passwordBox) {
                        start.linkTo(emailBox.start)
                        end.linkTo(emailBox.end)
                        top.linkTo(emailBox.bottom, margin = 12.dp)
                        width = Dimension.fillToConstraints
                    }
            ) {
                AuthTextField(
                    value = password,
                    error = passwordError,
                    onValueChange = onPasswordChange,
                    placeholder = "Пароль",
                    isPassword = true,
                    isLocked = isLoading,
                    iconResId = R.drawable.key_24,
                )
            }

            Box(
                modifier = Modifier
                    .constrainAs(buttonBox) {
                        start.linkTo(passwordBox.start, margin = horizontalMargin)
                        end.linkTo(passwordBox.end, margin = horizontalMargin)
                        top.linkTo(passwordBox.bottom, margin = 42.dp)
                        width = Dimension.fillToConstraints
                    }
            ) {
                AuthButton(
                    text = "Войти",
                    onClick = onLoginClick,
                    isLoaded = isLoading,
                    isLocked = isLocked,
                    modifier = Modifier
                )
            }

            Text(
                modifier = Modifier
                    .constrainAs(errorMsg) {
                        start.linkTo(buttonBox.start)
                        end.linkTo(buttonBox.end)
                        bottom.linkTo(buttonBox.top)
                        width = Dimension.fillToConstraints
                    },
                text = error,
                textAlign = TextAlign.Center,
                color = Color.Red,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            TextButton(
                onClick = onCreateAccountClick,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .constrainAs(createAccount) {
                        start.linkTo(buttonBox.start)
                        end.linkTo(buttonBox.end)
                        top.linkTo(buttonBox.bottom)
                    }
            ) {
                Text(
                    text = "Создать аккаунт",
                    color = Color(0xFFE8EAED).copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
            }
        }

    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2A2E37, name = "Email Empty")
@Composable
fun LoginElementPreview() {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isClick by remember { mutableStateOf(false) }
    BooqTheme {
        LoginElement(
            email = email,
            onEmailChange = { email = it },
            mailError = "",
            password = password,
            onPasswordChange = { password = it },
            isLoading = isClick,
            isLocked = false,
            error = "Привет как дела",
            onLoginClick = { isClick = true },
            onCreateAccountClick = { },
            modifier = Modifier
                .padding(10.dp)
        )
    }
}