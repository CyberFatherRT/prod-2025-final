package ru.prodcontest.booq.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ru.prodcontest.booq.R
import ru.prodcontest.booq.presentation.theme.BooqTheme

@Composable
fun HomeRegisterCompanyDialog(
    onDismissRequest: () -> Unit,
    comapnyId: String,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = modifier
                .clip(
                    RoundedCornerShape(16.dp)
                )
                .background(
                    color = Color.DarkGray
                )
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "!!! Внимание !!!",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Для вас сгененрированны авторизационные данные.",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )

                Spacer(Modifier.height(12.dp))

                Card(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 50.dp)
                ) {  }

                val modifierRow = Modifier.padding(10.dp).fillMaxWidth()
                RowInfoComponent(iconId = R.drawable.person_24, text = "${comapnyId}@nonexistentemail.com", modifier = modifierRow)
                RowInfoComponent(iconId = R.drawable.key_24, text = "password", modifier = modifierRow)


                Card(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 50.dp)
                ) {  }


                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .padding(horizontal = 42.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = "Далее",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier.height(1.dp))
            }
        }
    }
}

@Composable
private fun RowInfoComponent(
    iconId: Int,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(25.dp),
            painter = painterResource(iconId),
            contentDescription = null,
            tint = Color.Gray,
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Text(
            text = text,
            color = Color.Yellow,
            fontSize = 18.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2A2E37)
@Composable
fun HomeRegisterCompanyPreview() {
    BooqTheme {
        HomeRegisterCompanyDialog(
            modifier = Modifier
                .padding(12.dp),
            comapnyId = "TCS",
            onDismissRequest = { },
        )
    }
}