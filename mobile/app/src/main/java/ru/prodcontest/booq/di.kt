package ru.prodcontest.booq

import dagger.Module
import dagger.Provides
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

@Module
object AppModule {
    @Provides
    fun provideKtorClient(): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json()
        }
    }
}