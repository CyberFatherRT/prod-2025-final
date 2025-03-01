package ru.prodcontest.booq.domain.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

inline fun <reified R> wrapToResult(
    crossinline getData: suspend () -> R
): Flow<ResultWrapper<R>> =
    flow {
        emit(ResultWrapper.Loading)
        try {
            val resp = getData()
            emit(ResultWrapper.Ok(resp))
        } catch (e: Exception) {
            emit(ResultWrapper.Error(e.localizedMessage ?: e.message ?: e.toString(), e))
        }
    }