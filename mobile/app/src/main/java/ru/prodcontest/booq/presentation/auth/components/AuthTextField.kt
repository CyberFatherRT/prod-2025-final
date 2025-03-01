package ru.prodcontest.booq.presentation.auth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.xmlpull.v1.sax2.Driver
import ru.prodcontest.booq.R
import ru.prodcontest.booq.presentation.theme.BooqTheme

@Composable
fun AuthTextField(
    data: AuthTextData,
    isPassword: Boolean = false,
    iconResId: Int,
    isLocked: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val color = when {
        data.error.isNotEmpty() -> Color.Red
        isFocused && !isLocked -> MaterialTheme.colorScheme.primary
        else -> Color(0xFFE8EAED)
    }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 17.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = "Icon",
                tint = color,
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            BasicTextField(
                value = data.value,
                onValueChange = { if (!isLocked) data.onValueChange(it) },
                enabled = !isLocked,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                modifier = Modifier
                    .onFocusChanged { focusState ->
                        if (!isLocked) {
                            isFocused = focusState.isFocused
                        } else {
                            isFocused = false
                        }
                    }
                    .fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (data.value.isEmpty()) {
                        Text(
                            text = data.placeholder,
                            color = Color(0xFFE8EAED).copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    color = color
                )
        )

        Row(
            modifier = Modifier
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.fmd_bad_24),
                contentDescription = null,
                colorFilter = if (data.error.isNotEmpty()) ColorFilter.tint(Color.Red) else ColorFilter.tint(Color.Transparent),
                modifier = Modifier.size(12.dp)
            )

            Spacer(Modifier.width(2.dp))

            Text(
                text = data.error,
                color = Color.Red,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

data class AuthTextData(
    val value: String,
    val placeholder: String = "...",
    val onValueChange: (String) -> Unit,
    val error: String = ""
)

// Preview

@Preview(showBackground = true, backgroundColor = 0xFF3B3F48, name = "Email Empty")
@Composable
fun AuthTextFieldEmailPreview() {
    BooqTheme {
        var text by remember { mutableStateOf("") }
        AuthTextField(
            data = AuthTextData(
                value = text,
                onValueChange = { text = it },
                placeholder = "Почта"
            ),
            isLocked = true,
            iconResId = R.drawable.mail_24,
            modifier = Modifier
                .padding(horizontal = 17.dp, vertical = 12.dp)
        )
    }
}
