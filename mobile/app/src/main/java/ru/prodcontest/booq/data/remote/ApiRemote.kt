package ru.prodcontest.booq.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import ru.prodcontest.booq.data.remote.dto.LoginDto
import ru.prodcontest.booq.data.remote.dto.RegisterDto
import ru.prodcontest.booq.data.remote.dto.TokenDto

class ApiRemote(private val httpClient: HttpClient) {
    companion object {
        const val BASE_DOMAIN = "https://prod-team-13-cltnksuj.final.prodcontest.ru"
        const val LOGIN_ENDPOINT = "$BASE_DOMAIN/users/login"
        const val REGISTER_ENDPOINT = "$BASE_DOMAIN/users/register"
    }

    suspend fun login(creds: LoginDto) =
        httpClient.post(LOGIN_ENDPOINT) {
            setBody(creds)
        }.body<TokenDto>()

    suspend fun register(creds: RegisterDto) =
        httpClient.post(REGISTER_ENDPOINT) {
            setBody(creds)
        }.body<TokenDto>()
}
