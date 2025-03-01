package ru.prodcontest.booq.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import ru.prodcontest.booq.domain.model.AppConfig

interface AppConfigRepository {

    fun getFlowConfig(): Flow<AppConfig>

    suspend fun updateConfig(reducer: AppConfig.() -> AppConfig)

    fun getConfig(): AppConfig = runBlocking { getFlowConfig().first() }

}