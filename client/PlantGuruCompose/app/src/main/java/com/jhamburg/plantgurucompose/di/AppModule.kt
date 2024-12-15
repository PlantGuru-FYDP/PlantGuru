package com.jhamburg.plantgurucompose.di

import android.content.Context
import com.jhamburg.plantgurucompose.api.ApiService
import com.jhamburg.plantgurucompose.api.RetrofitInstance
import com.jhamburg.plantgurucompose.auth.AuthManager
import com.jhamburg.plantgurucompose.notifications.NotificationManager
import com.jhamburg.plantgurucompose.notifications.NotificationScheduler
import com.jhamburg.plantgurucompose.repository.PlantRepository
import com.jhamburg.plantgurucompose.repository.PredictionRepository
import com.jhamburg.plantgurucompose.repository.SensorDataRepository
import com.jhamburg.plantgurucompose.repository.UserRepository
import com.jhamburg.plantgurucompose.repository.WateringEventRepository
import com.jhamburg.plantgurucompose.viewmodels.PlantViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthManager(
        @ApplicationContext context: Context
    ): AuthManager {
        return AuthManager(context)
    }

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return RetrofitInstance.apiService
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService, authManager: AuthManager): UserRepository {
        return UserRepository(apiService, authManager)
    }

    @Provides
    @Singleton
    fun providePlantRepository(
        apiService: ApiService,
        @ApplicationContext context: Context
    ) = PlantRepository(apiService, context)

    @Provides
    @Singleton
    fun provideSensorDataRepository(apiService: ApiService) = SensorDataRepository(apiService)

    @Provides
    @Singleton
    fun provideWateringEventRepository(apiService: ApiService) = WateringEventRepository(apiService)

    @Provides
    @Singleton
    fun providePredictionRepository(apiService: ApiService) = PredictionRepository(apiService)

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager = NotificationManager(context)

    @Provides
    @Singleton
    fun provideNotificationScheduler(
        @ApplicationContext context: Context
    ): NotificationScheduler = NotificationScheduler(context)

    @Provides
    @Singleton
    fun providePlantViewModel(
        repository: PlantRepository,
        predictionRepository: PredictionRepository,
        @ApplicationContext context: Context,
        authManager: AuthManager
    ): PlantViewModel {
        return PlantViewModel(repository, predictionRepository, context, authManager)
    }
}
