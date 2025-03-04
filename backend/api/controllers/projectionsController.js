const SensorData = require("../models/sensorModel");

exports.getProjections = async (req, res) => {
    try {
        const { plant_id, sensor_type, num_points, granularity } = req.query;
        
        const validSensors = ['ext_temp', 'light', 'humidity', 'soil_temp', 'soil_moisture_1', 'soil_moisture_2'];
        if (!validSensors.includes(sensor_type)) {
            return res.status(400).send({ message: 'Invalid sensor type' });
        }

        const now = new Date();
        const timeRangeMs = granularity * 60 * 1000 * num_points;
        
        // Get historical data from the past day only
        const startTime = new Date(now.getTime() - (24 * 60 * 60 * 1000)); // 24 hours ago
        const historicalData = await SensorData.getTimeSeriesData(
            plant_id,
            startTime.toISOString(),
            now.toISOString(),
            granularity
        );

        if (!historicalData || historicalData.length === 0) {
            return res.status(404).send({ message: 'No sensor data found for this plant' });
        }

        // Filter to only include data from now or earlier
        const validHistoricalData = historicalData.filter(data => 
            new Date(data.time_period) <= now
        ).sort((a, b) => new Date(a.time_period) - new Date(b.time_period));

        if (validHistoricalData.length < 2) {
            return res.status(400).send({ message: 'Insufficient data for projections' });
        }

        // Calculate slope using last few points
        const numPointsForSlope = Math.min(6, validHistoricalData.length);
        const recentPoints = validHistoricalData.slice(-numPointsForSlope);
        
        // Calculate average rate of change per minute
        const firstPoint = recentPoints[0];
        const lastPoint = recentPoints[recentPoints.length - 1];
        const timeDiffMinutes = (new Date(lastPoint.time_period) - new Date(firstPoint.time_period)) / (60 * 1000);
        const valueDiff = lastPoint[sensor_type] - firstPoint[sensor_type];
        const slope = timeDiffMinutes > 0 ? (valueDiff / timeDiffMinutes) : 0;

        // Generate projections
        const projections = [];
        const lastHistoricalPoint = validHistoricalData[validHistoricalData.length - 1];
        let currentTime = new Date(lastHistoricalPoint.time_period);
        let currentValue = lastHistoricalPoint[sensor_type];

        // Add first projection point matching last historical point
        projections.push({
            value: currentValue,
            timestamp: currentTime.toISOString(),
            confidence: 1.0
        });

        // Generate remaining projections
        for (let i = 1; i < num_points; i++) {
            const minutesFromStart = granularity * i;
            let projectedValue = currentValue + (slope * minutesFromStart)-0.5;
            
            // Apply bounds
            projectedValue = boundProjectedValue(projectedValue, sensor_type);
            
            // Simple linear decay in confidence
            const confidence = Math.max(0.0, 1 - (2*i / num_points));
            
            projections.push({
                value: projectedValue,
                timestamp: new Date(currentTime.getTime() + (granularity * i * 60 * 1000)).toISOString(),
                confidence
            });
        }

        return res.status(200).send({
            plant_id,
            sensor_type,
            granularity,
            requested_points: Number(num_points),
            actual_points: projections.length,
            last_reading: {
                value: lastHistoricalPoint[sensor_type],
                timestamp: lastHistoricalPoint.time_period
            },
            historicalData: validHistoricalData,
            projections
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