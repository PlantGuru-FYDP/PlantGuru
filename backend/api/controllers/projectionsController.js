const SensorData = require("../models/sensorModel");
const { updatePlantPredictions } = require("../services/moisturePredictionModel");
const connection = require('../../db/connection');

exports.getProjections = async (req, res) => {
    try {
        const { plant_id, sensor_type, num_points, granularity, timestamp } = req.query;
        
        const validSensors = ['ext_temp', 'light', 'humidity', 'soil_temp', 'soil_moisture_1', 'soil_moisture_2'];
        if (!validSensors.includes(sensor_type)) {
            return res.status(400).send({ message: 'Invalid sensor type' });
        }

        // If timestamp is provided, use it as reference point, otherwise use current time
        const referenceTime = timestamp ? new Date(timestamp) : new Date();
        
        // Get predictions from the model
        const predictions = await updatePlantPredictions(
            connection,
            plant_id,
            Math.ceil(num_points * granularity / 60), // Convert to hours, rounding up
            timestamp ? new Date(timestamp) : null
        );

        if (!predictions) {
            return res.status(404).send({
                error: 'No predictions available for this plant'
            });
        }

        // Get historical data
        const endTime = predictions.referenceTime;
        const startTime = new Date(endTime.getTime() - (num_points * granularity * 60 * 1000));

        const [historicalData] = await connection.query(`
            WITH TimeGroups AS (
                SELECT FLOOR(UNIX_TIMESTAMP(time_stamp) / ?) AS time_group
                FROM SensorData
                WHERE plant_id = ?
                AND time_stamp BETWEEN ? AND ?
                GROUP BY FLOOR(UNIX_TIMESTAMP(time_stamp) / ?)
            )
            SELECT 
                FROM_UNIXTIME(time_group * ?) as time_period,
                AVG(ext_temp) as ext_temp,
                AVG(light) as light,
                AVG(humidity) as humidity,
                AVG(soil_temp) as soil_temp,
                AVG(soil_moisture_1) as soil_moisture_1,
                AVG(soil_moisture_2) as soil_moisture_2,
                COUNT(*) as data_points
            FROM SensorData
            JOIN TimeGroups ON FLOOR(UNIX_TIMESTAMP(time_stamp) / ?) = time_group
            WHERE plant_id = ?
            AND time_stamp BETWEEN ? AND ?
            GROUP BY time_group
            ORDER BY time_period ASC
        `, [granularity * 60, plant_id, startTime, endTime, granularity * 60, granularity * 60, granularity * 60, plant_id, startTime, endTime]);

        // Get the last reading for reference
        const [lastReading] = historicalData.slice(-1);

        // Generate projection points at the requested granularity
        const projectionPoints = [];
        const totalMinutes = num_points * granularity;
        const hoursToPredict = Math.ceil(totalMinutes / 60);
        
        console.log(`Debug - Projection parameters:
            totalMinutes: ${totalMinutes}
            hoursToPredict: ${hoursToPredict}
            available predictions: ${predictions.hourlyPredictions.length}
            granularity: ${granularity}
            num_points: ${num_points}`);
        
        // Calculate how many points we can actually generate
        const maxPoints = Math.floor((predictions.hourlyPredictions.length * 60) / granularity);
        const pointsToGenerate = Math.min(num_points, maxPoints);
        
        for (let i = 0; i < pointsToGenerate; i++) {
            const minute = i * granularity;
            const hourIndex = Math.floor(minute / 60);
            
            if (hourIndex >= predictions.hourlyPredictions.length - 1) {
                console.log(`Stopping at hour index ${hourIndex} (exceeds predictions)`);
                break;
            }
            
            const pointTime = new Date(predictions.referenceTime.getTime() + minute * 60 * 1000);
            const currentHour = predictions.hourlyPredictions[hourIndex];
            const nextHour = predictions.hourlyPredictions[hourIndex + 1];
            
            // Interpolate between hours
            const minuteOfHour = minute % 60;
            const hourProgress = minuteOfHour / 60;
            const interpolatedValue = currentHour.predicted_moisture + 
                (nextHour.predicted_moisture - currentHour.predicted_moisture) * hourProgress;
            
            projectionPoints.push({
                value: boundProjectedValue(interpolatedValue, sensor_type),
                timestamp: pointTime.toISOString(),
                confidence: Math.max(0.2, 1 - (i / pointsToGenerate))
            });
        }

        console.log(`Generated ${projectionPoints.length} points (requested: ${num_points}, max possible: ${maxPoints})`);

        // Send response
        res.status(200).send({
            plant_id: parseInt(plant_id),
            sensor_type,
            granularity,
            num_points,
            last_reading: {
                value: lastReading ? lastReading[sensor_type] : null,
                timestamp: lastReading ? lastReading.time_period : null
            },
            historicalData: historicalData.map(reading => ({
                value: reading[sensor_type],
                timestamp: reading.time_period,
                data_points: reading.data_points
            })),
            projections: projectionPoints,
            predicted_dry_time: predictions.predictedDryTime
        });
    } catch (err) {
        console.error('Error in getProjections:', err);
        return res.status(500).send({ message: err.message });
    }
};

function boundProjectedValue(value, sensorType) {
    switch (sensorType) {
        case 'humidity':
        case 'soil_moisture_1':
        case 'soil_moisture_2':
            return Math.max(0, Math.min(100, value));
        case 'light':
            return Math.max(0, Math.min(100000, value));
        case 'ext_temp':
        case 'soil_temp':
            return Math.max(-50, Math.min(100, value));
        default:
            return value;
    }
} 