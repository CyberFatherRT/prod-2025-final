package ru.prodcontest.booq.presentation.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ru.prodcontest.booq.R
import ru.prodcontest.booq.presentation.theme.BooqTheme

@Composable
fun HomeBookingCard(
    data: BookingDataUi,
    onBookingClick: () -> Unit,
    onBookingEditClick: () -> Unit,
    onDeleteClick:  () -> Unit,
    onQRClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(shape = RoundedCornerShape(32.dp))
            .background(color = MaterialTheme.colorScheme.onBackground, shape = RoundedCornerShape(32.dp))
            .clickable { onBookingClick() }
    ) {
        val (nameEl, lightEl, labelEl, infoEl, qrButtonEl, editButtonEl, deleteButtonEl) = createRefs()

        val sizeLight = 280.dp
        Box(
            modifier = Modifier
                .size(sizeLight)
                .constrainAs(lightEl) {
                    top.linkTo(parent.top, margin = -(sizeLight / 3)) // Смещение вверх
                    end.linkTo(parent.end, margin = -(sizeLight / 3))  // Смещение влево от конца
                }
        ) {
            val color = MaterialTheme.colorScheme.primary
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 1f),
                            color.copy(alpha = 0.7f),
                            color.copy(alpha = 0f)
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = sizeLight.toPx() / 2f
                    ),
                    radius = sizeLight.toPx() / 2f
                )
            }
        }
        
        Text(
            text = data.name,
            modifier = Modifier
                .constrainAs(nameEl) {
                    start.linkTo(parent.start, margin = 22.dp)
                    top.linkTo(parent.top, margin = 32.dp)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 32.sp
            ),
            maxLines = 1,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis
            
        )

        Text(
            text = data.label,
            modifier = Modifier
                .constrainAs(labelEl) {
                    start.linkTo(nameEl.start)
                    top.linkTo(nameEl.bottom, margin = 14.dp)
                    end.linkTo(lightEl.start)
                    width = Dimension.fillToConstraints
                },
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color.Black,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal
            ),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )

        val horizantalPaddingInfo = 18.dp

        Box(
            modifier = Modifier
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                )
                .drawWithContent {
                    drawContent()
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset(0f, 0f),
                        size = size,
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(10f, 10f),
                                phase = 0f
                            )
                        )
                    )
                }
                .fillMaxWidth()
                .wrapContentHeight()
                .constrainAs(infoEl) {
                    start.linkTo(parent.start, margin = horizantalPaddingInfo)
                    end.linkTo(parent.end, margin = horizantalPaddingInfo)
                    top.linkTo(lightEl.bottom, margin = 24.dp)
                    width = Dimension.fillToConstraints
                }
        ) {

            val modifierRow = Modifier.padding(10.dp)
            Column {
                RowInfoComponent(iconId = R.drawable.location_on_24, text = data.address, modifier = modifierRow)
                RowInfoComponent(iconId = R.drawable.door_open_24, text = data.space, modifier = modifierRow)
                RowInfoComponent(iconId = R.drawable.calendar_month_24, text = data.date, modifier = modifierRow)
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(color = Color.Red, shape = RoundedCornerShape(12.dp))
                .clickable { onDeleteClick() }
                .size(42.dp)
                .constrainAs(deleteButtonEl) {
                    end.linkTo(parent.end, margin = 22.dp)
                    top.linkTo(infoEl.bottom, margin = 22.dp)
                }
        ) {
            Icon(
                modifier = Modifier
                    .size(23.dp),
                painter = painterResource(R.drawable.delete_24),
                contentDescription = null,
                tint = Color.White
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(color = Color.Green, shape = RoundedCornerShape(12.dp))
                .clickable { onBookingEditClick() }
                .size(42.dp)
                .constrainAs(editButtonEl) {
                    end.linkTo(deleteButtonEl.start, margin = 4.dp)
                    top.linkTo(deleteButtonEl.top)
                }
        ) {
            Icon(
                modifier = Modifier
                    .size(23.dp),
                painter = painterResource(R.drawable.edit_24),
                contentDescription = null,
                tint = Color.White
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .clip(
                    RoundedCornerShape(12.dp)
                )
                .clickable { onQRClick() }
                .fillMaxWidth()
                .height(42.dp)
                .background(color = MaterialTheme.colorScheme.primary)
                .constrainAs(qrButtonEl) {
                    start.linkTo(parent.start, margin = 22.dp)
                    end.linkTo(editButtonEl.start, margin = 8.dp)
                    top.linkTo(editButtonEl.top)
                    width = Dimension.fillToConstraints
                }
        ) {
            Text(
                color = Color.Black,
                text = "Показать QR",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
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
            color = Color.Black,
            fontSize = 18.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

    }
}

data class BookingDataUi(
    val name: String,
    val label: String = "",
    val address: String,
    val date: String,
    val space: String
)

@Preview(showBackground = true, backgroundColor = 0xFF2A2E37)
@Composable
fun HomeBookingCardPreview() {
    BooqTheme {
        HomeBookingCard(
            data = BookingDataUi(
                name = "K-254",
                label = "- тянки\n- монитор\n- очень тихо",
                address = "Москва, ул. Ленина, д. 1\nИгровая комната 3",
                date = "13:00-15:00 12.12.2021",
                space = "Подтверждено"
            ),
            onBookingClick = {  },
            onBookingEditClick = { },
            onQRClick = { },
            onDeleteClick = { },
            modifier = Modifier
                .padding(horizontal = 20.dp)
        )
    }
}