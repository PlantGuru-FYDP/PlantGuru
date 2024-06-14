package com.example.plantguru.data

import com.example.plantguru.models.Plant

object LocalDataStore {
    fun getPlants(): List<Plant> {
        return listOf(
            Plant("b", "e")
        )
    }
}