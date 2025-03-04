package ru.prodcontest.booq.domain.usecase

import javax.inject.Inject
import kotlin.io.encoding.ExperimentalEncodingApi

class IsAdminUseCase @Inject constructor(
    val getTokenUseCase: GetTokenUseCase
) {
    @OptIn(ExperimentalEncodingApi::class)
    operator fun invoke(): Boolean {
        val token = getTokenUseCase()
        if (token != null ) {
            val body = java.util.Base64.getUrlDecoder().decode(token.split('.')[1]).decodeToString()
            return body.contains("ADMIN")
        } else {
            return false
        }
    }
}