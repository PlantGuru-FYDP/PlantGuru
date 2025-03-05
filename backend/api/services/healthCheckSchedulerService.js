const PlantHealthCheckService = require('./plantHealthCheckService');
const NotificationSettings = require('../models/notificationSettingsModel');
const Plant = require('../models/plantModel');

class HealthCheckSchedulerService {
    constructor() {
        this.HEALTH_CHECK_HOURS = Array.from({length: 24}, (_, i) => i);
        this.checkInterval = 1000 * 60 * 60; // Check every hour
        this.timer = null;
    }

    start() {
        if (this.timer) {
            clearInterval(this.timer);
        }

        const nextCheckTime = this.calculateNextCheckTime();
        console.log(`Scheduling next health check for ${new Date(Date.now() + nextCheckTime).toLocaleTimeString()}`);

        this.timer = setTimeout(() => {
            this.runHealthChecks();
            this.timer = setInterval(() => {
                this.runHealthChecks();
            }, this.checkInterval);
        }, nextCheckTime);

        console.log('Health check scheduler started');
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
        console.log('Health check scheduler stopped');
    }

    async runHealthChecks() {
        try {
            // Disabled health check notifications
            console.log('Health check notifications are disabled');
            return;
            
            // Original code commented out
            /*
            const currentTime = new Date().toLocaleTimeString();
            console.log(`Running health checks at ${currentTime}`);
            
            const settings = await NotificationSettings.getAllEnabledPlantSettings();
            console.log(`Found ${settings.length} plants with enabled notifications`);
            
            for (const plantSettings of settings) {
                await PlantHealthCheckService.checkPlantHealth(plantSettings.plant_id);
            }
            */
        } catch (error) {
            console.error('Error running health checks:', error);
        }
    }
}

module.exports = new HealthCheckSchedulerService(); 