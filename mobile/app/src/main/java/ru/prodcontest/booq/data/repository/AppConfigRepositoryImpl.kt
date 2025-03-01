package ru.prodcontest.booq.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.prodcontest.booq.domain.model.AppConfig
import ru.prodcontest.booq.domain.repository.AppConfigRepository

class AppConfigRepositoryImpl(
    @ApplicationContext private val context: Context
) : AppConfigRepository {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("config")
        val TOKEN_KEY = stringPreferencesKey("TOKEN")
    }

    override fun getFlowConfig(): Flow<AppConfig> =
        context.dataStore.data.map { prefs ->
            AppConfig(
                prefs[TOKEN_KEY]
            )
        }

    private fun <T> MutablePreferences.setOrRemove(key: Preferences.Key<T>, value: T?) {
        if (value != null) {
            this[key] = value
        } else {
            this.remove(key)
        }
    }

    override suspend fun updateConfig(reducer: AppConfig.() -> AppConfig) {
        val newConfig = getConfig().reducer()
        context.dataStore.edit {
            it.setOrRemove(TOKEN_KEY, newConfig.token)
        }
    }
}