package ru.prodcontest.booq.domain.util

sealed class UploadProgress {
    data object Completed : UploadProgress()
    data object Unknown : UploadProgress()
    data class Progress(val percent: Float) : UploadProgress()
    data class Error(val message: String) : UploadProgress()
}