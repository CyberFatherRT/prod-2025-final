package ru.prodcontest.booq.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.channels.ProducerScope
import ru.prodcontest.booq.data.remote.dto.BookingDto
import ru.prodcontest.booq.data.remote.dto.CompanyItemDto
import ru.prodcontest.booq.data.remote.dto.CoworkingDto
import ru.prodcontest.booq.data.remote.dto.CoworkingItemDto
import ru.prodcontest.booq.data.remote.dto.CreateBookingDto
import ru.prodcontest.booq.data.remote.dto.CreateBookingResponseDto
import ru.prodcontest.booq.data.remote.dto.ItemBookingDto
import ru.prodcontest.booq.data.remote.dto.LoginDto
import ru.prodcontest.booq.data.remote.dto.PatchBookingDto
import ru.prodcontest.booq.data.remote.dto.PlacesDto
import ru.prodcontest.booq.data.remote.dto.ProfileDto
import ru.prodcontest.booq.data.remote.dto.QrTokenDto
import ru.prodcontest.booq.data.remote.dto.QrVerificationDto
import ru.prodcontest.booq.data.remote.dto.RegisterCompanyDto
import ru.prodcontest.booq.data.remote.dto.RegisterDto
import ru.prodcontest.booq.data.remote.dto.TokenDto
import ru.prodcontest.booq.data.remote.dto.VerificationDto
import ru.prodcontest.booq.data.remote.dto.VerificationUserDto
import ru.prodcontest.booq.domain.util.UploadProgress

class ApiRemote(private val httpClient: HttpClient) {
    companion object {
        const val BASE_DOMAIN = "https://prod-team-13-cltnksuj.final.prodcontest.ru/backend_api"
        const val LOGIN_ENDPOINT = "$BASE_DOMAIN/user/login"
        const val REGISTER_ENDPOINT = "$BASE_DOMAIN/user/register"
        const val PROFILE_ENDPOINT = "$BASE_DOMAIN/user/profile"
        const val VERIFICATIONS_ENDPOINT = "$BASE_DOMAIN/admin/list_requests"
        const val REGISTER_COMPANY_ENDPOINT = "$BASE_DOMAIN/company/register"
        const val UPLOAD_DOCUMENT_ENDPOINT = "$BASE_DOMAIN/user/upload_document"
        const val BOOKING_LIST_ENDPOINT = "$BASE_DOMAIN/booking/list"
        const val VERIFY_BOOKING_QR_ENDPOINT = "$BASE_DOMAIN/booking/verify"
        const val LIST_PLACES_ENDPOINT = "$BASE_DOMAIN/place/list"
        const val BOOKING_QR_ENDPOINT = "$BASE_DOMAIN/booking/{booking_id}/qr"
        const val LIST_COWORKINGS_OF_BUILDING_ENDPOINT =
            "$BASE_DOMAIN/place/{building_id}/coworking/list"
        const val LIST_ITEMS_OF_COMPANY_ENDPOINT = "$BASE_DOMAIN/items"
        const val LIST_ITEMS_OF_COWORKING_ENDPOINT =
            "$BASE_DOMAIN/place/{building_id}/coworking/{coworking_id}/items"
        const val LIST_BOOKINGS_OF_COWORKING_ENDPOINT =
            "$BASE_DOMAIN/place/{building_id}/coworking/{coworking_id}/bookings"
        const val CREATE_BOOKING_ENDPOINT = "$BASE_DOMAIN/booking/create"
        const val UPDATE_BOOKING_ENDPOINT = "$BASE_DOMAIN/booking/{booking_id}"
        const val DELETE_BOOKING_ENDPOINT = "$BASE_DOMAIN/booking/{booking_id}"
        const val VERIFY_GUEST_ENDPOINT = "$BASE_DOMAIN/admin/user/{user_id}/verify"
        const val DECLINE_GUEST_ENDPOINT = "$BASE_DOMAIN/admin/user/{user_id}"
        const val COWORKING_LIST_ENDPOINT = "$BASE_DOMAIN/place/coworking/list"
        const val LIST_USERS_ENDPOINT = "$BASE_DOMAIN/admin/user/list"
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

    suspend fun getProfile() = httpClient.get(PROFILE_ENDPOINT).body<ProfileDto>()
    suspend fun verifications() =
        httpClient.get(VERIFICATIONS_ENDPOINT).body<List<VerificationDto>>()


    suspend fun registerCompany(creds: RegisterCompanyDto) =
        httpClient.post(REGISTER_COMPANY_ENDPOINT) {
            setBody(creds)
            contentType(ContentType.Application.Json)
            attributes.put(InsertAuthAttrs.DontInsert, true)
        }.body<TokenDto>()

    suspend fun uploadDocument(document: ByteArray, flow: ProducerScope<UploadProgress>) {
        val res = try {
            val res = httpClient.submitFormWithBinaryData(
                url = UPLOAD_DOCUMENT_ENDPOINT, formData = formData {
                    append("document", document, Headers.build {
                        append(HttpHeaders.ContentType, "application/pdf")
                        append(HttpHeaders.ContentDisposition, "filename=\"doc.pdf\"")
                    })
                }) {
                onUpload { sent, total ->
                    total?.let {
                        flow.send(UploadProgress.Progress((sent / total) * 100f))
                    } ?: run {
                        flow.send(UploadProgress.Unknown)
                    }
                }
            }
            if (res.status.isSuccess()) {
                UploadProgress.Completed
            } else {
                UploadProgress.Error(res.bodyAsText())
            }
        } catch (e: Exception) {
            UploadProgress.Error(e.localizedMessage ?: e.message ?: e.toString())
        }
        flow.send(res)
    }

    suspend fun getBookingList() =
        httpClient.get(BOOKING_LIST_ENDPOINT) {
            contentType(ContentType.Application.Json)
        }.body<List<BookingDto>>()


    suspend fun verifyBookingQr(token: String) = httpClient.post(VERIFY_BOOKING_QR_ENDPOINT) {
        contentType(ContentType.Application.Json)
        setBody(mapOf("token" to token))
    }.body<QrVerificationDto>()

    suspend fun listPlaces() = httpClient.get(LIST_PLACES_ENDPOINT).body<PlacesDto>()

    suspend fun getQr(bookingId: String) =
        httpClient.get(BOOKING_QR_ENDPOINT.replace("{booking_id}", bookingId)) {
            contentType(ContentType.Application.Json)
        }.body<QrTokenDto>()

    suspend fun getCoworkingsOfBuilding(buildingId: String) =
        httpClient.get(LIST_COWORKINGS_OF_BUILDING_ENDPOINT.replace("{building_id}", buildingId))
            .body<List<CoworkingDto>>()

    suspend fun getItemsOfCompany() =
        httpClient.get(LIST_ITEMS_OF_COMPANY_ENDPOINT).body<List<CompanyItemDto>>()

    suspend fun getItemsOfCoworking(buildingId: String, coworkingId: String) = httpClient.get(
        LIST_ITEMS_OF_COWORKING_ENDPOINT
            .replace("{building_id}", buildingId)
            .replace("{coworking_id}", coworkingId)
    ).body<List<CoworkingItemDto>>()

    suspend fun approveUserVerification(userId: String) = httpClient.post(
        VERIFY_GUEST_ENDPOINT
            .replace("{user_id}", userId)
    ).status.value

    suspend fun declineUserVerification(userId: String) = httpClient.delete(
        DECLINE_GUEST_ENDPOINT
            .replace("{user_id}", userId)
    ).status.value

    suspend fun getBookingsOfCoworking(buildingId: String, coworkingId: String) = httpClient.get(
        LIST_BOOKINGS_OF_COWORKING_ENDPOINT
            .replace("{building_id}", buildingId)
            .replace("{coworking_id}", coworkingId)
    ).body<List<ItemBookingDto>>()

    suspend fun createBooking(createBookingDto: CreateBookingDto) =
        httpClient.post(CREATE_BOOKING_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(createBookingDto)
        }.body<CreateBookingResponseDto>()

    suspend fun listCoworkings() =
        httpClient.get(COWORKING_LIST_ENDPOINT).body<List<CoworkingDto>>()

    suspend fun updateBooking(bookingId: String, dto: PatchBookingDto) =
        httpClient.patch(UPDATE_BOOKING_ENDPOINT.replace("{booking_id}", bookingId)) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body<CreateBookingResponseDto>()

    suspend fun deleteBooking(bookingId: String) =
        httpClient.delete(DELETE_BOOKING_ENDPOINT.replace("{booking_id}", bookingId)) {
            contentType(ContentType.Application.Json)
        }.status.value

    suspend fun listUsers() = httpClient.get(
        LIST_USERS_ENDPOINT
    ).body<List<VerificationUserDto>>()
}
