const SensorData = require("../models/sensorModel");

exports.getProjections = async (req, res) => {
    // Filler projection for now, just a simple linear based on last value with decreasing confidence
    try {
        const { plant_id, sensor_type, num_points, granularity } = req.query;
        
        const validSensors = ['ext_temp', 'light', 'humidity', 'soil_temp', 'soil_moisture_1', 'soil_moisture_2'];
        if (!validSensors.includes(sensor_type)) {
            return res.status(400).send({ message: 'Invalid sensor type' });
        }

        const [firstData] = await SensorData.readFirstData(plant_id);
        const [latestData] = await SensorData.readLatestData(plant_id);
        
        if (!latestData || latestData.length === 0 || !firstData || firstData.length === 0) {
            return res.status(404).send({ message: 'No sensor data found for this plant' });
        }

        const totalHistoricalSpan = Math.floor(
            (new Date(latestData[0].time_stamp) - new Date(firstData[0].time_stamp)) / (60 * 1000)
        );

        const maxPoints = Math.floor(totalHistoricalSpan / granularity);
        const adjustedNumPoints = Math.min(num_points, maxPoints);

        const lastValue = latestData[0][sensor_type];
        const now = new Date();
        
        const timeRangeMs = granularity * 60 * 1000 * adjustedNumPoints;
        const startTime = new Date(now.getTime() - timeRangeMs);

        const historicalData = await SensorData.getTimeSeriesData(
            plant_id,
            startTime.toISOString(),
            new Date(now.getTime() + timeRangeMs).toISOString(),
            granularity
        );

        let slope = 0;
        if (historicalData.length >= 2) {
            const lastPoint = historicalData[historicalData.length - 1];
            const secondLastPoint = historicalData[historicalData.length - 2];
            const valueDiff = lastPoint[sensor_type] - secondLastPoint[sensor_type];
            slope = valueDiff / granularity;
        }

        const projections = [];
        let currentTime = new Date(now);
        const intervalMs = granularity * 60 * 1000;
        let currentValue = historicalData.length > 0 
            ? historicalData[historicalData.length - 1][sensor_type]
            : latestData[0][sensor_type];

        for (let i = 0; i < adjustedNumPoints; i++) {
            const minutesFromNow = granularity * i;
            const projectedValue = currentValue + (slope * minutesFromNow);
            projections.push({
                value: projectedValue,
                timestamp: currentTime.toISOString(),
                confidence: 1 - (i / adjustedNumPoints)
            });
            currentTime = new Date(currentTime.getTime() + intervalMs);
        }

        return res.status(200).send({
            plant_id,
            sensor_type,
            granularity,
            requested_points: Number(num_points),
            actual_points: adjustedNumPoints,
            total_historical_minutes: totalHistoricalSpan,
            last_reading: {
                value: lastValue,
                timestamp: latestData[0].time_stamp
            },
            historicalData,
            projections
        });
    } catch (err) {
        console.error('Error in getProjections:', err);
        return res.status(500).send({ message: err.message });
    }
}; 