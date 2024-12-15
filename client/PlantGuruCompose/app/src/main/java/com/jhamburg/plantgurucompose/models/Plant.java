package com.jhamburg.plantgurucompose.models;

public class Plant {
    public String name;
    public String deviceUUID;
    public Boolean connected = false;

    // Constructor
    public Plant(String deviceUUID, String plantName) {
        this.name = plantName;
        this.deviceUUID = deviceUUID;
    }

    // Getters
    public String getPlantName() {
        return name;
    }

    // Setters
    public void setPlantName(String plantName) {
        this.name = plantName;
    }
}