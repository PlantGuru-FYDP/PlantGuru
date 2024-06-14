package com.example.plantguru.data

import com.example.plantguru.models.Plant

class PlantRepository {
    suspend fun getPlants(): List<Plant> {
        return LocalDataStore.getPlants()
    }
}