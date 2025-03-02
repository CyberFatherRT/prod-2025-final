package ru.prodcontest.booq.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ru.prodcontest.booq.data.remote.dto.LoginDto
import ru.prodcontest.booq.data.remote.dto.ProfileDto
import ru.prodcontest.booq.data.remote.dto.RegisterCompanyDto
import ru.prodcontest.booq.data.remote.dto.RegisterDto
import ru.prodcontest.booq.data.remote.dto.TokenDto
import ru.prodcontest.booq.data.remote.dto.VerificationDto

class ApiRemote(private val httpClient: HttpClient) {
    companion object {
        const val BASE_DOMAIN = "https://prod-team-13-cltnksuj.final.prodcontest.ru"
        const val LOGIN_ENDPOINT = "$BASE_DOMAIN/user/login"
        const val REGISTER_ENDPOINT = "$BASE_DOMAIN/user/register"
        const val PROFILE_ENDPOINT = "$BASE_DOMAIN/user/profile"
        const val VERIFICATIONS_ENDPOINT = "$BASE_DOMAIN/admin/list_requests"
        const val REGISTER_COMPANY_ENDPOINT = "$BASE_DOMAIN/company/register"
    }

    suspend fun login(creds: LoginDto) =
        httpClient.post(LOGIN_ENDPOINT) {
            setBody(creds)
            contentType(ContentType.Application.Json)
            attributes.put(InsertAuthAttrs.DontInsert, true)
        }.body<TokenDto>()

    suspend fun register(creds: RegisterDto) =
        httpClient.post(REGISTER_ENDPOINT) {
            setBody(creds)
            contentType(ContentType.Application.Json)
            attributes.put(InsertAuthAttrs.DontInsert, true)
        }.body<TokenDto>()

    suspend fun profile() = httpClient.get(PROFILE_ENDPOINT).body<ProfileDto>()
    suspend fun verifications() = httpClient.get(VERIFICATIONS_ENDPOINT).body<List<VerificationDto>>()


    suspend fun registerCompany(creds: RegisterCompanyDto) =
        httpClient.post(REGISTER_COMPANY_ENDPOINT) {
            setBody(creds)
            contentType(ContentType.Application.Json)
            attributes.put(InsertAuthAttrs.DontInsert, true)
        }.body<TokenDto>()

}
