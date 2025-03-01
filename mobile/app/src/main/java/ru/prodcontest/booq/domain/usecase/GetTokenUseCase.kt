package ru.prodcontest.booq.domain.usecase

import ru.prodcontest.booq.domain.repository.AppConfigRepository
import javax.inject.Inject

class GetTokenUseCase @Inject constructor(private val appConfigRepository: AppConfigRepository) {
    operator fun invoke(): String? = appConfigRepository.getConfig().token
}