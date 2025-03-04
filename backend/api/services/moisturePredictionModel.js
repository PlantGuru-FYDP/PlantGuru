const { Matrix } = require('ml-matrix');

class ARMAXPredictor {
    constructor(config = {}) {
        this.useExternalInputs = config.useExternalInputs ?? false;
        this.ignoreWindowHours = config.ignoreWindowHours ?? 20;
        this.maWindow = config.maWindow ?? 5;
        this.minDecrease = config.minDecrease ?? 0.0001;
        this.beta = null;
        this.pastErrors = null;
        this.regressionWindow = 30; // minutes for slope calculation
    }

    /**
     * Compute regression slope over a sliding window
     * @param {Array<number>} values - Array of moisture values
     * @param {number} window - Window size in minutes
     * @returns {Array<number>} Array of slopes
     */
    computeRegressionSlope(values, window = this.regressionWindow) {
        const slopes = new Array(values.length).fill(0);
        const x = Array.from({ length: window }, (_, i) => i);
        
        for (let i = window; i <= values.length; i++) {
            const windowValues = values.slice(i - window, i);
            if (windowValues.length === window) {
                // Simple linear regression
                const xMean = (window - 1) / 2;
                const yMean = windowValues.reduce((a, b) => a + b) / window;
                let numerator = 0;
                let denominator = 0;
                
                for (let j = 0; j < window; j++) {
                    numerator += (j - xMean) * (windowValues[j] - yMean);
                    denominator += (j - xMean) * (j - xMean);
                }
                
                slopes[i - 1] = numerator / denominator;
            }
        }
        
        return slopes;
    }

    /**
     * Train the ARMAX model on historical data
     * @param {Array<Object>} sensorData - Array of sensor readings
     */
    async train(sensorData) {
        // Filter out data in ignore window
        const validData = sensorData.filter(reading => 
            !reading.in_ignore_window && 
            reading.hours_since_watering >= this.ignoreWindowHours
        );

        if (validData.length < this.regressionWindow) {
            console.warn('Insufficient training data');
            this.beta = new Array(this.useExternalInputs ? 7 : 4).fill(0);
            return;
        }

        // Compute regression slope
        const moistureValues = validData.map(d => d.soil_moisture_1);
        const slopes = this.computeRegressionSlope(moistureValues);

        // Initialize past errors
        this.pastErrors = new Array(validData.length).fill(0);

        // Iteratively improve model with moving average errors
        for (let iteration = 0; iteration < 3; iteration++) {
            const X = [];
            const y = [];

            for (let t = this.regressionWindow; t < validData.length; t++) {
                // Compute moving average of past errors
                const maError = this.pastErrors
                    .slice(Math.max(0, t - this.maWindow), t)
                    .reduce((a, b) => a + b, 0) / this.maWindow;

                const row = [
                    1, // constant term
                    validData[t - 1].soil_moisture_1, // previous moisture
                    slopes[t - 1], // previous slope
                    maError // moving average of past errors
                ];

                if (this.useExternalInputs) {
                    row.push(
                        validData[t].light,
                        validData[t].humidity,
                        validData[t].ext_temp
                    );
                }

                X.push(row);
                y.push(validData[t].soil_moisture_1);
            }

            if (X.length === 0) {
                console.warn('No valid training points');
                return;
            }

            // Add regularization constraint
            const regStrength = 1.0;
            const constraintX = new Array(X[0].length).fill(0);
            constraintX[1] = regStrength; // coefficient for previous moisture
            X.push(constraintX);
            y.push(0.99 * regStrength);

            // Solve using normal equations with SVD
            const Xmatrix = new Matrix(X);
            const yVector = Matrix.columnVector(y);
            
            try {
                const Xt = Xmatrix.transpose();
                const XtX = Xt.mmul(Xmatrix);
                const Xty = Xt.mmul(yVector);
                
                // Add regularization to make the system more stable
                for (let i = 0; i < XtX.rows; i++) {
                    XtX.set(i, i, XtX.get(i, i) + 1e-6);
                }
                
                // Solve the system using built-in solve method
                try {
                    this.beta = XtX.solve(Xty).to1DArray();
                } catch (solveError) {
                    console.warn('Matrix solve failed, using fallback method');
                    // Fallback to simple persistence model
                    this.beta = new Array(X[0].length).fill(0);
                    this.beta[1] = 0.99; // Previous moisture coefficient
                }

                // Update past errors for next iteration
                const predictions = X.slice(0, -1).map(row => 
                    row.reduce((sum, val, idx) => sum + val * this.beta[idx], 0)
                );
                this.pastErrors.splice(this.regressionWindow, predictions.length, 
                    ...predictions.map((pred, idx) => y[idx] - pred));
            } catch (error) {
                console.error('Error solving linear system:', error);
                // Fallback to simple persistence model
                this.beta = new Array(X[0].length).fill(0);
                this.beta[1] = 0.99; // Previous moisture coefficient
            }
        }
    }

    /**
     * Make predictions for future moisture values
     * @param {Object} currentData - Current sensor reading
     * @param {number} horizonMinutes - Number of minutes to predict into future
     * @returns {Array<number>} Predicted moisture values
     */
    predict(currentData, horizonMinutes) {
        if (!this.beta) {
            throw new Error('Model not trained');
        }

        const predictions = new Array(horizonMinutes);
        let lastMoisture = currentData.soil_moisture_1;
        let lastSlope = currentData.slope || 0;
        let lastMaError = this.pastErrors ? 
            this.pastErrors.slice(-this.maWindow).reduce((a, b) => a + b, 0) / this.maWindow : 
            0;

        const externalInputs = this.useExternalInputs ? {
            light: currentData.light,
            humidity: currentData.humidity,
            ext_temp: currentData.ext_temp
        } : null;

        for (let i = 0; i < horizonMinutes; i++) {
            const row = [
                1,
                lastMoisture,
                lastSlope,
                lastMaError
            ];

            if (this.useExternalInputs) {
                row.push(
                    externalInputs.light,
                    externalInputs.humidity,
                    externalInputs.ext_temp
                );
            }

            // Make prediction
            let pred = row.reduce((sum, val, idx) => sum + val * this.beta[idx], 0);

            // Apply constraints
            pred = Math.min(pred, lastMoisture); // Can't increase
            pred = Math.max(pred, 0); // Can't go negative
            pred = Math.max(pred, lastMoisture - 0.01); // Max 1% decrease per minute

            // Apply minimum decrease constraint
            if (this.minDecrease > 0) {
                const maxAllowed = lastMoisture - this.minDecrease;
                pred = Math.min(pred, maxAllowed);
            }

            predictions[i] = pred;

            // Update for next prediction
            lastMoisture = pred;
            lastSlope = pred - predictions[Math.max(0, i - 1)];
            lastMaError *= 0.9; // Decay past errors
        }

        return predictions;
    }
}

/**
 * Get drying cycles from sensor data and watering events
 * @param {Array<Object>} sensorData - Array of sensor readings
 * @param {Array<Object>} wateringEvents - Array of watering events
 * @returns {Array<Array<Object>>} Array of drying cycles
 */
function getDryingCycles(sensorData, wateringEvents) {
    // Handle empty input cases
    if (!Array.isArray(sensorData) || !Array.isArray(wateringEvents) || sensorData.length === 0) {
        return [];
    }

    const cycles = [];
    let currentCycle = [];
    
    // Sort watering events by time
    const sortedEvents = [...wateringEvents].sort((a, b) => 
        new Date(a.time_stamp) - new Date(b.time_stamp)
    );
    
    // Add a final "virtual" watering event after the last reading
    const lastTime = new Date(sensorData[sensorData.length - 1].time_stamp);
    sortedEvents.push({ time_stamp: new Date(lastTime.getTime() + 1) });
    
    // Get the first watering event's time or use the first reading's time
    let lastWateringTime = sortedEvents.length > 0 ? 
        new Date(sortedEvents[0].time_stamp) : 
        new Date(sensorData[0].time_stamp);
    
    // Process each reading
    for (const reading of sensorData) {
        const readingTime = new Date(reading.time_stamp);
        
        // Find the next watering event
        while (sortedEvents.length > 0 && new Date(sortedEvents[0].time_stamp) <= readingTime) {
            // End current cycle and start a new one
            if (currentCycle.length > 0) {
                cycles.push(currentCycle);
                currentCycle = [];
            }
            lastWateringTime = new Date(sortedEvents[0].time_stamp);
            sortedEvents.shift();
        }
        
        // Add reading to current cycle with hours since last watering
        if (sortedEvents.length > 0) {
            currentCycle.push({
                ...reading,
                hours_since_watering: (readingTime - lastWateringTime) / (1000 * 60 * 60)
            });
        }
    }
    
    // Add the last cycle if it has data
    if (currentCycle.length > 0) {
        cycles.push(currentCycle);
    }
    
    return cycles;
}

/**
 * Service function to update predictions for a plant
 * @param {Object} db - Database connection
 * @param {number} plantId - Plant ID
 * @param {number} predictionHours - Hours to predict into future
 */
async function updatePlantPredictions(db, plantId, predictionHours = 24) {
    try {
        // Get all sensor data (we'll filter to relevant cycles later)
        const [sensorData] = await db.query(`
            SELECT 
                time_stamp,
                soil_moisture_1,
                light,
                humidity,
                ext_temp
            FROM SensorData
            WHERE plant_id = ?
            AND soil_moisture_1 IS NOT NULL  -- Ensure we have moisture readings
            ORDER BY time_stamp ASC
        `, [plantId]);

        if (!sensorData || sensorData.length === 0) {
            console.log(`No sensor data found for plant ${plantId}`);
            return;
        }

        // Get all watering events
        const [wateringEvents] = await db.query(`
            SELECT time_stamp
            FROM WateringEvent
            WHERE plant_id = ?
            ORDER BY time_stamp ASC
        `, [plantId]);

        // Get drying cycles
        const cycles = getDryingCycles(sensorData, wateringEvents);
        
        if (cycles.length < 2) {  // Need at least 2 cycles (1 for training, 1 for current)
            console.log(`Insufficient cycles for plant ${plantId} (only ${cycles.length} cycles)`);
            return;
        }

        // Get plant's moisture threshold from notification settings
        const [settings] = await db.query(`
            SELECT soil_moisture_min
            FROM PlantNotificationSettings
            WHERE plant_id = ?
        `, [plantId]);

        const moistureThreshold = settings.length > 0 ? settings[0].soil_moisture_min : 20;

        // Mark ignore windows in each cycle
        const ignoreWindowHours = 20;
        cycles.forEach(cycle => {
            cycle.forEach(reading => {
                reading.in_ignore_window = reading.hours_since_watering <= ignoreWindowHours;
            });
        });

        // Use last 3 complete cycles for training (excluding current cycle)
        const trainingCycles = cycles.slice(-4, -1);  // Take 3 cycles before the current one
        const currentCycle = cycles[cycles.length - 1];

        // Combine training cycles
        const trainingData = trainingCycles.flat();

        if (trainingData.length < 30) {  // Need minimum points for meaningful prediction
            console.log(`Insufficient training data for plant ${plantId}`);
            return;
        }

        // Initialize and train model
        const model = new ARMAXPredictor({
            useExternalInputs: false,
            ignoreWindowHours: 20,
            minDecrease: 0.0001
        });

        await model.train(trainingData);

        // Get current conditions (last point after ignore window in current cycle)
        const validCurrentData = currentCycle.filter(reading => 
            !reading.in_ignore_window && 
            reading.hours_since_watering >= ignoreWindowHours
        );

        if (validCurrentData.length === 0) {
            console.log(`No valid current data after ignore window for plant ${plantId}`);
            return;
        }

        const currentReading = validCurrentData[validCurrentData.length - 1];
        
        // Make predictions
        const predictions = model.predict(currentReading, predictionHours * 60);

        // Store hourly predictions
        const hourlyPredictions = predictions
            .filter((_, i) => i % 60 === 0)
            .map((moisture, hour) => [
                plantId,
                moisture,
                new Date(new Date(currentReading.time_stamp).getTime() + hour * 60 * 60 * 1000)
            ]);

        if (hourlyPredictions.length > 0) {
            await db.query(`
                INSERT INTO HourlyMoisturePredictions 
                (plant_id, predicted_moisture, prediction_for_time)
                VALUES ?
            `, [hourlyPredictions]);
        }

        // Find when moisture will drop below threshold
        const dryIndex = predictions.findIndex(moisture => moisture < moistureThreshold);
        if (dryIndex !== -1) {
            const dryTime = new Date(new Date(currentReading.time_stamp).getTime() + dryIndex * 60 * 1000);
            await db.query(`
                INSERT INTO MoisturePredictions 
                (plant_id, predicted_dry_time, current_moisture)
                VALUES (?, ?, ?)
            `, [plantId, dryTime, currentReading.soil_moisture_1]);
        }

    } catch (error) {
        console.error(`Error updating predictions for plant ${plantId}:`, error);
        throw error;
    }
}

module.exports = {
    ARMAXPredictor,
    updatePlantPredictions,
    getDryingCycles  // Export for testing
};