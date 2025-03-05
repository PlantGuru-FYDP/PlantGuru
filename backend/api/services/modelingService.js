const SensorData = require("../models/sensorModel");
const Plant = require("../models/plantModel");
const connection = require('../../db/connection');
const { updatePlantPredictions } = require('./moisturePredictionModel');

class ModelingService {
    constructor() {
        this.checkInterval = 1000 * 60 * 60; // Check every hour (milliseconds * seconds * minutes)
        this.timer = null;
    }

    async start() {
        if (this.timer) {
            clearInterval(this.timer);
        }

        // Run immediately on startup
        console.log('Running initial moisture predictions update on startup...');
        await this.updatePredictions();

        const nextCheckTime = this.calculateNextCheckTime();
        console.log(`Scheduling next moisture prediction update for ${new Date(Date.now() + nextCheckTime).toLocaleTimeString()}`);

        this.timer = setTimeout(() => {
            this.updatePredictions();
            this.timer = setInterval(() => {
                this.updatePredictions();
            }, this.checkInterval);
        }, nextCheckTime);

        console.log('Moisture prediction scheduler started');
    }

    calculateNextCheckTime() {
        const now = new Date();
        const next = new Date(now);
        next.setHours(next.getHours() + 1, 0, 0, 0);
        return next.getTime() - now.getTime();
    }

    stop() {
        if (this.timer) {
            clearInterval(this.timer);
            this.timer = null;
        }
        console.log('Moisture prediction scheduler stopped');
    }

    async updatePredictions() {
        try {
            console.log('Running moisture predictions update...');
            // Get all plants
            const [plants] = await Plant.readAllPlants();
            
            for (const plant of plants) {
                await this.updatePlantPredictions(plant.plant_id);
            }
            console.log('Moisture predictions update completed successfully');
        } catch (error) {
            console.error('Error in updatePredictions:', error);
        }
    }

    async updatePlantPredictions(plant_id) {
        try {
            return await updatePlantPredictions(connection, plant_id, 24);
        } catch (error) {
            console.error(`Error updating predictions for plant ${plant_id}:`, error);
            return null;
        }
    }

    async getNextDryTime(plant_id) {
        try {
            const [predictions] = await connection.query(
                `SELECT predicted_dry_time 
                 FROM MoisturePredictions 
                 WHERE plant_id = ? 
                 ORDER BY prediction_created_at DESC 
                 LIMIT 1`,
                [plant_id]
            );

            return predictions[0]?.predicted_dry_time || null;
        } catch (error) {
            console.error('Error in getNextDryTime:', error);
            return null;
        }
    }

    async getHourlyPredictions(plant_id) {
        try {
            const [predictions] = await connection.query(
                `SELECT predicted_moisture, prediction_for_time 
                 FROM HourlyMoisturePredictions 
                 WHERE plant_id = ? 
                 AND prediction_for_time > NOW()
                 ORDER BY prediction_for_time ASC`,
                [plant_id]
            );

            return predictions;
        } catch (error) {
            console.error('Error in getHourlyPredictions:', error);
            return [];
        }
    }
}

module.exports = new ModelingService(); 