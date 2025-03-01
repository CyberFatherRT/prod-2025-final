package ru.prodcontest.booq.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.prodcontest.booq.data.repository.AppConfigRepositoryImpl
import ru.prodcontest.booq.domain.repository.AppConfigRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    // ============================================================
    //                         App Settings
    // ============================================================

    @Provides
    @Singleton
    fun provideAppConfigRepository(
        @ApplicationContext context: Context
    ): AppConfigRepository {
        return AppConfigRepositoryImpl(context)
    }

    // ============================================================
    //                            Room
    // ============================================================

    // CODE CODE CODE

    // ============================================================
    //                            ktor
    // ============================================================

}