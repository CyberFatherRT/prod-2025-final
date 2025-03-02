package ru.prodcontest.booq.presentation.verifications

import ru.prodcontest.booq.domain.model.VerificationModel

data class VerificationsScreenState(
    val verificationsInfo: List<VerificationModel>?,
    val isLoading: Boolean,
    val error: String?
)
