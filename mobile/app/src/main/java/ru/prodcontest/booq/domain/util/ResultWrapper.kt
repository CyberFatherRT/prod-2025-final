package ru.prodcontest.booq.domain.util

sealed class ResultWrapper<out T> {
    data class Ok<T>(val data: T) : ResultWrapper<T>()
    data class Error(val message: String, val cause: Throwable? = null) : ResultWrapper<Nothing>()
}
