package ru.prodcontest.booq.presentation.showQR

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import qrgenerator.QRCodeImage

@Composable
fun ShowQrScreen(modifier: Modifier = Modifier) {
    QRCodeImage("https://ya.ru", null)
}