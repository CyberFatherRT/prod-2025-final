package ru.prodcontest.booq.data.repository

import ru.prodcontest.booq.data.remote.ApiRemote
import ru.prodcontest.booq.data.remote.dto.LoginDto
import ru.prodcontest.booq.data.remote.dto.RegisterCompanyDto
import ru.prodcontest.booq.data.remote.dto.RegisterDto
import ru.prodcontest.booq.data.remote.dto.TokenDto
import ru.prodcontest.booq.domain.model.UserModel
import ru.prodcontest.booq.domain.model.VerificationModel
import ru.prodcontest.booq.domain.repository.ApiRepository
import ru.prodcontest.booq.domain.util.ResultFlow
import ru.prodcontest.booq.domain.util.wrapToResult

class ApiRepositoryImpl(private val apiRemote: ApiRemote) : ApiRepository {
    override suspend fun login(creds: LoginDto): ResultFlow<TokenDto> =
        wrapToResult { apiRemote.login(creds) }

    override suspend fun register(creds: RegisterDto): ResultFlow<TokenDto> =
        wrapToResult { apiRemote.register(creds) }

    override suspend fun getProfile(): ResultFlow<UserModel> = wrapToResult {
        apiRemote.profile().toModel()
    }

    override suspend fun getVerifications(): ResultFlow<List<VerificationModel>> = wrapToResult {
        apiRemote.verifications().map { it.toModel() }
    }

    override suspend fun registerCompany(creds: RegisterCompanyDto): ResultFlow<TokenDto> =
        wrapToResult { apiRemote.registerCompany(creds) }

}
