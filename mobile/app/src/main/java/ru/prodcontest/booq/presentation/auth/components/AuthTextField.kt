package ru.prodcontest.booq.presentation.auth.components

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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.xmlpull.v1.sax2.Driver
import ru.prodcontest.booq.R
import ru.prodcontest.booq.presentation.theme.BooqTheme

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    iconResId: Int,
    modifier: Modifier = Modifier
) {
    val isFocused by remember { mutableStateOf(false) }

    val color = if (isFocused) MaterialTheme.colorScheme.primary else Color(0xFFE8EAED)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    decorationBox = { innerTextField ->

                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = color,
                                fontSize = 16.sp
                            )
                        }
                    }
                )
        }
        Box(
            modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    color = color
                )
        )
    }

}

@Preview(showBackground = true, backgroundColor = 0xFF3B3F48, name = "Email Empty")
@Composable
fun AuthTextFieldEmailEmptyPreview() {
    BooqTheme {
        var text by remember { mutableStateOf("") }
        AuthTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = "Email",
            iconResId = R.drawable.mail_24
        )
    }
}