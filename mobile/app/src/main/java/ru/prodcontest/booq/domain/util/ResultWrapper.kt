package ru.prodcontest.booq.domain.util

import kotlinx.coroutines.flow.Flow

sealed class ResultWrapper<out T> {
    data class Ok<T>(val data: T) : ResultWrapper<T>()
    data class Error(val message: String, val cause: Throwable? = null) : ResultWrapper<Nothing>()
    object Loading : ResultWrapper<Nothing>()
}

typealias ResultFlow<T> = Flow<ResultWrapper<T>>
