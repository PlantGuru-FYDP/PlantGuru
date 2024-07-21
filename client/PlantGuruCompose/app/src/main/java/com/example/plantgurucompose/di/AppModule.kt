package com.example.plantgurucompose.di

import com.example.plantgurucompose.api.ApiService
import com.example.plantgurucompose.api.RetrofitInstance
import com.example.plantgurucompose.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return RetrofitInstance.apiService
    }

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService) = UserRepository(apiService)

    @Provides
    @Singleton
    fun providePlantRepository(apiService: ApiService) = PlantRepository(apiService)

    @Provides
    @Singleton
    fun provideSensorDataRepository(apiService: ApiService) = SensorDataRepository(apiService)

    @Provides
    @Singleton
    fun provideWateringEventRepository(apiService: ApiService) = WateringEventRepository(apiService)
}
