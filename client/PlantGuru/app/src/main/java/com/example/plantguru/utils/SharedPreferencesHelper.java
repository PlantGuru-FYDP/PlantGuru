package com.example.plantguru.utils;
import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import com.example.plantguru.models.Plant;

public class SharedPreferencesHelper {
    private static final String PREFS_NAME = "provisioned_plants2";
    private static final Gson gson = new Gson();

    public static void savePlant(Context context, Plant plant) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = gson.toJson(plant);
        prefs.edit().putString(plant.getPlantName(), json).apply();
    }

    public static List<Plant> getPlants(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        List<Plant> plantList = new ArrayList<>();
        for (String key : prefs.getAll().keySet()) {
            String json = prefs.getString(key, "");
            Plant plant = gson.fromJson(json, Plant.class);
            plantList.add(plant);
        }
        return plantList;
    }

    public static void clearAllPlants(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}