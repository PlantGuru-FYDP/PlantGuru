const { Matrix, SVD } = require('ml-matrix');

class ARMAXPredictor {
    constructor(config = {}) {
        this.useExternalInputs = config.useExternalInputs ?? false;
        this.ignoreWindowHours = config.ignoreWindowHours ?? 20;
        this.maWindow = config.maWindow ?? 1;
        this.minDecrease = config.minDecrease ?? 0.01;
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
        console.log(`Starting ARMAX model training with ${sensorData.length} data points`);
        
        // Filter out data in ignore window
        const validData = sensorData.filter(reading => 
            !reading.in_ignore_window && 
            reading.hours_since_watering >= this.ignoreWindowHours
        );
        console.log(`Found ${validData.length} valid data points after filtering ignore window`);

        if (validData.length < this.regressionWindow) {
            console.warn(`Insufficient training data: ${validData.length} points < ${this.regressionWindow} required`);
            this.beta = new Array(this.useExternalInputs ? 7 : 4).fill(0);
            return;
        }

        // Compute regression slope
        const moistureValues = validData.map(d => d.soil_moisture_1);
        const slopes = this.computeRegressionSlope(moistureValues);
        console.log(`Computed regression slopes for ${slopes.length} points`);

        // Initialize past errors
        this.pastErrors = new Array(validData.length).fill(0);

        // Iteratively improve model with moving average errors
        for (let iteration = 0; iteration < 3; iteration++) {
            console.log(`Starting iteration ${iteration + 1}/3 of model training`);
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

            console.log(`Built training matrices: X(${X.length}x${X[0]?.length || 0}), y(${y.length})`);

            if (X.length === 0) {
                console.warn('No valid training points after matrix construction');
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
                console.log('Attempting to solve matrix equation...');
                const Xt = Xmatrix.transpose();
                const XtX = Xt.mmul(Xmatrix);
                
                // Log matrix condition
                console.log('Matrix dimensions:', {
                    X: `${Xmatrix.rows}x${Xmatrix.columns}`,
                    Xt: `${Xt.rows}x${Xt.columns}`,
                    XtX: `${XtX.rows}x${XtX.columns}`
                });

                // Add regularization to make the system more stable
                for (let i = 0; i < XtX.rows; i++) {
                    XtX.set(i, i, XtX.get(i, i) + 1e-6);
                }
                
                const Xty = Xt.mmul(yVector);
                
                try {
                    // Use SVD to solve the system
                    const svd = new SVD(XtX);
                    this.beta = svd.solve(Xty).to1DArray();
                    
                    // Log matrix properties using SVD
                    const condition = svd.condition;
                    const rank = svd.rank;
                    
                    console.log('Matrix properties:', {
                        condition,
                        rank,
                        singular_values: svd.diagonal
                    });
                    
                    console.log('Successfully solved matrix equation. Beta coefficients:', this.beta);
                } catch (solveError) {
                    console.warn('Matrix solve failed:', solveError.message);
                    // Fallback to simple persistence model
                    this.beta = new Array(X[0].length).fill(0);
                    this.beta[1] = 0.99; // Previous moisture coefficient
                    console.log('Using fallback persistence model with beta:', this.beta);
                }

                // Update past errors for next iteration
                if (this.beta) {
                    const predictions = X.slice(0, -1).map(row => 
                        row.reduce((sum, val, idx) => sum + val * this.beta[idx], 0)
                    );
                    this.pastErrors.splice(this.regressionWindow, predictions.length, 
                        ...predictions.map((pred, idx) => y[idx] - pred));
                    
                    // Log prediction errors
                    const mse = predictions.reduce((sum, pred, idx) => 
                        sum + Math.pow(y[idx] - pred, 2), 0) / predictions.length;
                    console.log(`Iteration ${iteration + 1} MSE: ${mse.toFixed(4)}`);
                }
            } catch (error) {
                console.error('Error in matrix operations:', error);
                console.log('Matrix state:', {
                    X_shape: [Xmatrix.rows, Xmatrix.columns],
                    y_shape: [yVector.rows, yVector.columns],
                    X_values: X.slice(0, 2),
                    y_values: y.slice(0, 2)
                });
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
 * @returns {Object|null} Predictions object
 */
async function updatePlantPredictions(db, plantId, predictionHours = 24) {
    try {
        console.log(`Running moisture predictions update for plant ${plantId}...`);
        
        // Get all sensor data
        const [sensorData] = await db.query(`
            SELECT 
                time_stamp,
                soil_moisture_1,
                light,
                humidity,
                ext_temp
            FROM SensorData
            WHERE plant_id = ?
            AND soil_moisture_1 IS NOT NULL
            ORDER BY time_stamp ASC
        `, [plantId]);

        console.log(`Retrieved ${sensorData.length} sensor readings for plant ${plantId}`);

        if (!sensorData || sensorData.length === 0) {
            console.log(`No sensor data found for plant ${plantId}`);
            return null;
        }

        // Get watering events
        const [wateringEvents] = await db.query(`
            SELECT time_stamp
            FROM WateringEvent
            WHERE plant_id = ?
            ORDER BY time_stamp ASC
        `, [plantId]);

        console.log(`Retrieved ${wateringEvents.length} watering events for plant ${plantId}`);

        // Get drying cycles
        const cycles = getDryingCycles(sensorData, wateringEvents);
        console.log(`Identified ${cycles.length} drying cycles`);
        
        if (cycles.length < 1) {  // Need at least 1 cycle for training
            console.log(`Insufficient cycles for plant ${plantId} (only ${cycles.length} cycles)`);
            return null;
        }

        // Get plant's moisture threshold from notification settings
        const [settings] = await db.query(`
            SELECT soil_moisture_min, soil_moisture_notifications
            FROM PlantNotificationSettings
            WHERE plant_id = ?
        `, [plantId]);

        const defaultThreshold = 40; // More conservative default threshold
        const moistureThreshold = settings.length > 0 ? 
            (settings[0].soil_moisture_min ?? defaultThreshold) : 
            defaultThreshold;
        
        console.log(`Moisture threshold settings for plant ${plantId}:`, {
            hasSettings: settings.length > 0,
            notificationsEnabled: settings[0]?.soil_moisture_notifications,
            configuredThreshold: settings[0]?.soil_moisture_min,
            usingThreshold: moistureThreshold,
            isDefault: settings.length === 0 || settings[0].soil_moisture_min === null
        });

        // Mark ignore windows in each cycle
        const ignoreWindowHours = 20;
        cycles.forEach(cycle => {
            cycle.forEach(reading => {
                reading.in_ignore_window = reading.hours_since_watering <= ignoreWindowHours;
            });
        });

        // Get the last complete cycle for training
        const trainingCycle = cycles[cycles.length - 2] || cycles[cycles.length - 1];
        const currentCycle = cycles[cycles.length - 1];
        console.log(`Using last complete cycle for training with ${trainingCycle.length} points`);

        if (trainingCycle.length < 30) {  // Need minimum points for meaningful prediction
            console.log(`Insufficient training data for plant ${plantId}`);
            return null;
        }

        // Initialize and train model
        const model = new ARMAXPredictor({
            useExternalInputs: false,
            ignoreWindowHours: 20,
            minDecrease: 0.0002
        });

        await model.train([trainingCycle]); // Train on just the last complete cycle
        console.log('Model training completed');

        // Get current reading - if in ignore window, use the last reading from current cycle
        const currentReading = currentCycle[currentCycle.length - 1];
        console.log(`Using current reading from ${currentReading.time_stamp} with moisture ${currentReading.soil_moisture_1}%`);
        
        // Make predictions
        const predictions = model.predict(currentReading, predictionHours * 60);
        console.log(`Generated ${predictions.length} predictions`);

        const referenceTime = new Date(currentReading.time_stamp);
        
        const hourlyPredictions = predictions
            .filter((_, i) => i % 60 === 0)
            .map((moisture, hour) => ({
                predicted_moisture: moisture,
                prediction_for_time: new Date(referenceTime.getTime() + hour * 60 * 60 * 1000)
            }));

        // Find when moisture will drop below threshold
        const dryIndex = predictions.findIndex(moisture => moisture < moistureThreshold);
        const predictedDryTime = dryIndex !== -1 ? 
            new Date(referenceTime.getTime() + dryIndex * 60 * 1000) : 
            null;

        // Store predictions in database
        if (hourlyPredictions.length > 0) {
            // Store hourly predictions
            const hourlyPredictionValues = hourlyPredictions.map(pred => [
                plantId,
                pred.predicted_moisture,
                pred.prediction_for_time
            ]);

            await db.query(`
                INSERT INTO HourlyMoisturePredictions 
                (plant_id, predicted_moisture, prediction_for_time)
                VALUES ?
            `, [hourlyPredictionValues]);

            // Store dry time prediction if available
            if (predictedDryTime) {
                await db.query(`
                    INSERT INTO MoisturePredictions 
                    (plant_id, predicted_dry_time, current_moisture)
                    VALUES (?, ?, ?)
                `, [plantId, predictedDryTime, currentReading.soil_moisture_1]);
            }
        }

        console.log(`Prediction complete. Dry time: ${predictedDryTime ? predictedDryTime.toISOString() : 'not predicted'}`);

        return {
            hourlyPredictions,
            predictedDryTime,
            currentMoisture: currentReading.soil_moisture_1,
            referenceTime
        };
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