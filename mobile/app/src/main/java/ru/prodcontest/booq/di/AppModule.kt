package ru.prodcontest.booq.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import ru.prodcontest.booq.data.remote.ApiRemote
import ru.prodcontest.booq.data.remote.InsertAuthKtorPlugin
import ru.prodcontest.booq.data.repository.ApiRepositoryImpl
import ru.prodcontest.booq.data.repository.AppConfigRepositoryImpl
import ru.prodcontest.booq.domain.repository.ApiRepository
import ru.prodcontest.booq.domain.repository.AppConfigRepository
import ru.prodcontest.booq.domain.usecase.GetTokenUseCase
import ru.prodcontest.booq.domain.usecase.SetTokenUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    // =============================================
    //                  USERCASE
    // =============================================

    @Provides
    @Singleton
    fun provideAppConfigRepository(
        @ApplicationContext context: Context
    ): AppConfigRepository {
        return AppConfigRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideHttpClient(useCase: GetTokenUseCase): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json()
        }
        install(InsertAuthKtorPlugin) {
            getTokenUseCase=useCase
        }
    }


    @Provides
    @Singleton
    fun provideApiRemote(httpClient: HttpClient) = ApiRemote(httpClient)

    @Provides
    @Singleton
    fun provideApiRepository(apiRemote: ApiRemote): ApiRepository = ApiRepositoryImpl(apiRemote)
}