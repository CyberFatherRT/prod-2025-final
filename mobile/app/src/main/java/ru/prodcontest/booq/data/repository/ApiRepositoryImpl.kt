package ru.prodcontest.booq.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import ru.prodcontest.booq.data.remote.ApiRemote
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
import ru.prodcontest.booq.domain.repository.ApiRepository
import ru.prodcontest.booq.domain.util.ResultFlow
import ru.prodcontest.booq.domain.util.UploadProgress
import ru.prodcontest.booq.domain.util.wrapToResult

class ApiRepositoryImpl(private val apiRemote: ApiRemote) : ApiRepository {
    override suspend fun login(creds: LoginDto): ResultFlow<TokenDto> = wrapToResult {
        apiRemote.login(creds)
    }

    override suspend fun register(creds: RegisterDto): ResultFlow<TokenDto> = wrapToResult {
        apiRemote.register(creds)
    }

    override suspend fun getProfile(): ResultFlow<UserModel> = wrapToResult {
        apiRemote.getProfile().toModel()
    }

    override suspend fun getVerifications(): ResultFlow<List<VerificationModel>> = wrapToResult {
        apiRemote.verifications().map { it.toModel() }
    }

    override suspend fun registerCompany(creds: RegisterCompanyDto): ResultFlow<TokenDto> =
        wrapToResult {
            apiRemote.registerCompany(creds)
        }

    override suspend fun uploadDocument(document: ByteArray): Flow<UploadProgress> = channelFlow {
        apiRemote.uploadDocument(document, this)
    }

    override suspend fun getBookingList(): ResultFlow<List<BookingModel>> =
        wrapToResult { apiRemote.getBookingList().map { it.toModel() } }

    override suspend fun verifyBookingQr(token: String): ResultFlow<QrVerificationModel> =
        wrapToResult {
            apiRemote.verifyBookingQr(token).toModel()
        }

    override suspend fun listPlaces(): ResultFlow<PlacesDto> = wrapToResult {
        apiRemote.listPlaces()
    }

    override suspend fun getQr(bookingId: String): ResultFlow<QrTokenDto> = wrapToResult {
        apiRemote.getQr(bookingId)
    }

    override suspend fun getCoworkingsOfBuilding(buildingId: String): ResultFlow<List<CoworkingDto>> =
        wrapToResult {
            apiRemote.getCoworkingsOfBuilding(buildingId)
        }

    override suspend fun getItemsOfCompany(): ResultFlow<List<CompanyItemDto>> = wrapToResult {
        apiRemote.getItemsOfCompany()
    }

    override suspend fun getItemsOfCoworking(
        buildingId: String,
        coworkingId: String
    ): ResultFlow<List<CoworkingItemDto>> = wrapToResult {
        apiRemote.getItemsOfCoworking(buildingId, coworkingId)
    }

    override suspend fun verifyGuest(userId: String): ResultFlow<Int> = wrapToResult {
        apiRemote.approveUserVerification(userId)
    }

    override suspend fun declineGuest(userId: String): ResultFlow<Int> = wrapToResult {
        apiRemote.declineUserVerification(userId)
    }

    override suspend fun getBookingsOfCoworking(
        buildingId: String,
        coworkingId: String
    ): ResultFlow<List<ItemBookingDto>> = wrapToResult {
        apiRemote.getBookingsOfCoworking(buildingId, coworkingId)
    }

    override suspend fun createBooking(createBookingDto: CreateBookingDto): ResultFlow<CreateBookingResponseDto> = wrapToResult {
        apiRemote.createBooking(createBookingDto)
    }

    override suspend fun updateBooking(bookingId: String, patchBookingDto: PatchBookingDto): ResultFlow<CreateBookingResponseDto>  = wrapToResult {
        apiRemote.updateBooking(bookingId, patchBookingDto)
    }

    override suspend fun listCoworkings(): ResultFlow<List<CoworkingDto>> = wrapToResult {
        apiRemote.listCoworkings()
    }

    override suspend fun deleteBooking(bookingId: String): ResultFlow<Int> = wrapToResult {
        apiRemote.deleteBooking(bookingId)
    }

    override suspend fun listUsers(): ResultFlow<List<VerificationUserModel>> = wrapToResult {
        apiRemote.listUsers() .map { it.toModel() }
    }
}
