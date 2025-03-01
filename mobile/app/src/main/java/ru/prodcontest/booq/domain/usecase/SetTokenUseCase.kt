package ru.prodcontest.booq.domain.usecase

import ru.prodcontest.booq.domain.repository.AppConfigRepository
import javax.inject.Inject

class SetTokenUseCase @Inject constructor(private val appConfigRepository: AppConfigRepository) {
    suspend operator fun invoke(token: String) =
        appConfigRepository.updateConfig { copy(token = token) }
}
