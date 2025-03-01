package ru.prodcontest.booq.domain.repository

import ru.prodcontest.booq.data.remote.dto.LoginDto
import ru.prodcontest.booq.data.remote.dto.RegisterDto
import ru.prodcontest.booq.data.remote.dto.TokenDto
import ru.prodcontest.booq.domain.model.UserModel
import ru.prodcontest.booq.domain.util.ResultFlow

interface ApiRepository {
    suspend fun login(creds: LoginDto): ResultFlow<TokenDto>
    suspend fun register(creds: RegisterDto): ResultFlow<TokenDto>
    suspend fun getProfile(): ResultFlow<UserModel>
}