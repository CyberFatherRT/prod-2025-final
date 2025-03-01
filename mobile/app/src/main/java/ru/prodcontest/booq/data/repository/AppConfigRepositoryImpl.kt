package ru.prodcontest.booq.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import ru.prodcontest.booq.domain.model.AppConfig
import ru.prodcontest.booq.domain.repository.AppConfigRepository
import java.io.File

class AppConfigRepositoryImpl(
    @ApplicationContext private val context: Context
) : AppConfigRepository {

    companion object {
        private const val CONFIG_FILENAME = "config.json"
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val configFile = File(context.filesDir, CONFIG_FILENAME)

    private var config = loadInitialConfig()

    private val _configFlow = MutableStateFlow(config)
    private val configFlow = _configFlow.asStateFlow()

    init {
        if (!configFile.exists()) {
            copyConfigFromAssets()
            config = loadInitialConfig()
            _configFlow.value = config
        }
    }

    override fun getFlowConfig(): StateFlow<AppConfig> = configFlow

    override suspend fun updateConfig(newConfig: AppConfig) {
        withContext(Dispatchers.IO) {
            saveConfig(newConfig)
            _configFlow.emit(newConfig)
        }
    }

    override fun getConfig(): AppConfig = config

    private fun loadInitialConfig(): AppConfig {
        return try {
            json.decodeFromString(configFile.readText())
        } catch (e: Exception) {
            json.decodeFromString(context.assets.open(CONFIG_FILENAME).bufferedReader().use { it.readText() })
        }
    }

    private fun saveConfig(newConfig: AppConfig) {
        config = newConfig
        configFile.writeText(json.encodeToString(newConfig))
    }

    private fun copyConfigFromAssets() {
        context.assets.open(CONFIG_FILENAME).use { input ->
            configFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}