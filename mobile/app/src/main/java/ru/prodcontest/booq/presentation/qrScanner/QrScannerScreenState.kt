package ru.prodcontest.booq.presentation.qrScanner

import ru.prodcontest.booq.domain.model.QrVerificationModel

data class QrScannerScreenState(
    val loadingVerdict: Boolean,
    val errorVerdict: String?,
    val verdict: QrVerificationModel?,
    val modalOpened: Boolean
)
