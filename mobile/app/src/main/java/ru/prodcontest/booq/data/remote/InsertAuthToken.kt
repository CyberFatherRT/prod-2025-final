package ru.prodcontest.booq.data.remote

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.util.AttributeKey
import ru.prodcontest.booq.domain.usecase.GetTokenUseCase


class InsertAuthKtorPluginConfig {
    var getTokenUseCase: GetTokenUseCase? = null
}

val InsertAuthKtorPlugin =
    createClientPlugin("InsertAuthKtorPlugin", ::InsertAuthKtorPluginConfig) {
        onRequest { request, _ ->
            val token = this@createClientPlugin.pluginConfig.getTokenUseCase?.invoke()

            if (!request.attributes.contains(InsertAuthAttrs.DontInsert)) {
                token?.let {
                    request.headers.apply {
                        append("Authorization", "Bearer $it")
                    }
                }
                if (token == null) {
                    throw NoTokenException()
                }
            }
        }
    }

object InsertAuthAttrs {
    val DontInsert = AttributeKey<Boolean>("DontInsert")
}
class NoTokenException : Exception("No token provided")