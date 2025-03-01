package ru.prodcontest.booq.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ru.prodcontest.booq.domain.model.AppConfig

interface AppConfigRepository {

    fun getFlowConfig(): StateFlow<AppConfig>

    suspend fun updateConfig(newConfig: AppConfig)

    fun getConfig(): AppConfig

}