package ru.prodcontest.booq.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.prodcontest.booq.data.remote.dto.CompanyItemDto
import ru.prodcontest.booq.data.remote.dto.CoworkingDto
import ru.prodcontest.booq.data.remote.dto.CoworkingItemDto
import ru.prodcontest.booq.data.remote.dto.CreateBookingDto
import ru.prodcontest.booq.data.remote.dto.CreateBookingResponseDto
import ru.prodcontest.booq.data.remote.dto.ItemBookingDto
import ru.prodcontest.booq.data.remote.dto.LoginDto
import ru.prodcontest.booq.data.remote.dto.PatchBookingDto
import ru.prodcontest.booq.data.remote.dto.PlacesDto
import ru.prodcontest.booq.data.remote.dto.QrTokenDto
import ru.prodcontest.booq.data.remote.dto.RegisterCompanyDto
import ru.prodcontest.booq.data.remote.dto.RegisterDto
import ru.prodcontest.booq.data.remote.dto.TokenDto
import ru.prodcontest.booq.domain.model.BookingModel
import ru.prodcontest.booq.domain.model.QrVerificationModel
import ru.prodcontest.booq.domain.model.UserModel
import ru.prodcontest.booq.domain.model.VerificationModel
import ru.prodcontest.booq.domain.model.VerificationUserModel
import ru.prodcontest.booq.domain.util.ResultFlow
import ru.prodcontest.booq.domain.util.UploadProgress

interface ApiRepository {
    suspend fun login(creds: LoginDto): ResultFlow<TokenDto>
    suspend fun register(creds: RegisterDto): ResultFlow<TokenDto>
    suspend fun getProfile(): ResultFlow<UserModel>
    suspend fun getVerifications(): ResultFlow<List<VerificationModel>>
    suspend fun registerCompany(creds: RegisterCompanyDto): ResultFlow<TokenDto>
    suspend fun uploadDocument(document: ByteArray): Flow<UploadProgress>
    suspend fun getBookingList(): ResultFlow<List<BookingModel>>
    suspend fun verifyBookingQr(token: String): ResultFlow<QrVerificationModel>
    suspend fun listPlaces(): ResultFlow<PlacesDto>
    suspend fun getCoworkingsOfBuilding(buildingId: String): ResultFlow<List<CoworkingDto>>
    suspend fun getItemsOfCompany(): ResultFlow<List<CompanyItemDto>>
    suspend fun getItemsOfCoworking(buildingId: String, coworkingId: String): ResultFlow<List<CoworkingItemDto>>
    suspend fun getQr(bookingId: String): ResultFlow<QrTokenDto>
    suspend fun verifyGuest(userId: String): ResultFlow<Int>
    suspend fun declineGuest(userId: String): ResultFlow<Int>
    suspend fun getBookingsOfCoworking(buildingId: String, coworkingId: String): ResultFlow<List<ItemBookingDto>>
    suspend fun createBooking(createBookingDto: CreateBookingDto): ResultFlow<CreateBookingResponseDto>
    suspend fun deleteBooking(bookingId: String): ResultFlow<Int>
    suspend fun updateBooking(bookingId: String, patchBookingDto: PatchBookingDto): ResultFlow<CreateBookingResponseDto>
    suspend fun listCoworkings(): ResultFlow<List<CoworkingDto>>
    suspend fun listUsers(): ResultFlow<List<VerificationUserModel>>
}