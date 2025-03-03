package ru.prodcontest.booq.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.prodcontest.booq.data.remote.dto.LoginDto
import ru.prodcontest.booq.data.remote.dto.PlacesDto
import ru.prodcontest.booq.data.remote.dto.RegisterCompanyDto
import ru.prodcontest.booq.data.remote.dto.RegisterDto
import ru.prodcontest.booq.data.remote.dto.TokenDto
import ru.prodcontest.booq.domain.model.BookingModel
import ru.prodcontest.booq.domain.model.QrVerificationModel
import ru.prodcontest.booq.domain.model.UserModel
import ru.prodcontest.booq.domain.model.VerificationModel
import ru.prodcontest.booq.domain.util.ResultFlow
import ru.prodcontest.booq.domain.util.ResultStateFlow
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
    suspend fun getQr(bookingId: String): ResultFlow<TokenDto>
}