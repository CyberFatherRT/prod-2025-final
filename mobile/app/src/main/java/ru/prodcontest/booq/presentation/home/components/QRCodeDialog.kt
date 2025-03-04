package ru.prodcontest.booq.presentation.home.components


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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import com.lightspark.composeqr.DotShape
import com.lightspark.composeqr.QrCodeColors
import com.lightspark.composeqr.QrCodeView
import org.xmlpull.v1.sax2.Driver
import ru.prodcontest.booq.R
import ru.prodcontest.booq.presentation.theme.BooqTheme

@Composable
fun QRCodeDialog(
    onDismissRequest: () -> Unit,
    data: QRCodeDialogUiModel,
    qrCodeText: String,
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
                    text = data.name,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 50.dp)
                ) {  }

                if (qrCodeText.isNotEmpty()) {
                    Box(
                        modifier =  Modifier
                            .size(230.dp)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp)
                            ),

                        ) {
                        QrCodeView(
                            data = qrCodeText,
                            modifier = Modifier.size(230.dp).padding(8.dp),
                            colors = QrCodeColors(
                                background = Color.White,
                                foreground = Color.Black
                            ),
                            dotShape = DotShape.Circle
                        )
                    }
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =  Modifier
                            .size(230.dp)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(42.dp),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Card(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 50.dp)
                ) {  }

                val modifierRow = Modifier.padding(10.dp).fillMaxWidth()
                RowInfoComponent(iconId = R.drawable.location_on_24, text = data.address, modifier = modifierRow)
                RowInfoComponent(iconId = R.drawable.calendar_month_24, text = data.time, modifier = modifierRow)


                Card(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 50.dp)
                ) {  }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

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
                        text = "Закрыть",
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
            color = Color.White,
            fontSize = 18.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2A2E37)
@Composable
fun QRCodeDialogPreview() {
    BooqTheme {
        QRCodeDialog(
            modifier = Modifier
                .padding(12.dp),
            data = QRCodeDialogUiModel(
                name = "K-A-123",
                address = "ул. Колотушкина, д. 123",
                time = "13:00 01.02.2029",
            ),
            onDismissRequest = { },
            qrCodeText = "adsjfksadhlflkajshdflaskjdaslkdjflaskdjflk;dgjd;sflgjs;flgjsj;dlgf"
        )
    }
}

data class QRCodeDialogUiModel(
    val name: String,
    val address: String,
    val time: String,
)