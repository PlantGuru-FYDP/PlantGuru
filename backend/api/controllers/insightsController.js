const SensorData = require("../models/sensorModel");
const WateringEvent = require("../models/wateringEventModel");
const Plant = require("../models/plantModel");

const calculateSensorHealth = (value, optimalRanges) => {
  if (value === null || value === undefined) return 'UNKNOWN';
  
  const { min, max, criticalMin, criticalMax } = optimalRanges;
  
  if (value <= criticalMin || value >= criticalMax) return 'CRITICAL';
  if (value < min) return 'WARNING_LOW';
  if (value > max) return 'WARNING_HIGH';
  return 'GOOD';
};

const OPTIMAL_RANGES = {
  ext_temp: {
    criticalMin: 0,
    min: 15,
    max: 30,
    criticalMax: 40,
    unit: '°C'
  },
  light: {
    criticalMin: 0,
    min: 1000,
    max: 10000,
    criticalMax: 15000,
    unit: 'lux'
  },
  humidity: {
    criticalMin: 20,
    min: 40,
    max: 70,
    criticalMax: 90,
    unit: '%'
  },
  soil_temp: {
    criticalMin: 5,
    min: 15,
    max: 25,
    criticalMax: 35,
    unit: '°C'
  },
  soil_moisture_1: {
    criticalMin: 10,
    min: 30,
    max: 70,
    criticalMax: 90,
    unit: '%'
  },
  soil_moisture_2: {
    criticalMin: 10,
    min: 30,
    max: 70,
    criticalMax: 90,
    unit: '%'
  }
};

const validatePlantId = async (plant_id) => {
  if (!plant_id) throw new Error('Plant ID is required');
  const [plant] = await Plant.readDataById(plant_id);
  if (!plant || plant.length === 0) throw new Error('Plant not found');
  return plant[0];
};

const getValidatedSensorData = async (plant_id) => {
  const [latestData] = await SensorData.readLatestData(plant_id);
  if (!latestData || latestData.length === 0) {
    throw new Error('No sensor data found for this plant');
  }
  return latestData[0];
};


function getSeason(date) {
    const month = date.getMonth();
    if (month >= 2 && month <= 4) return 'SPRING';
    if (month >= 5 && month <= 7) return 'SUMMER';
    if (month >= 8 && month <= 10) return 'FALL';
    return 'WINTER';
  }
  
  function getAdjustedRanges(baseRanges, hour, season) {
    const ranges = { ...baseRanges };
    
    if (ranges.unit === 'lux') {
      if (hour < 6 || hour > 20) { 
        ranges.min = 0;
        ranges.max = 100;
      } else if (hour < 8 || hour > 18) {
        ranges.min = ranges.min * 0.3;
        ranges.max = ranges.max * 0.3;
      }
    }
  
    if (ranges.unit === '°C') {
      if (season === 'WINTER') {
        ranges.min -= 2;
        ranges.max -= 2;
      } else if (season === 'SUMMER') {
        ranges.min += 2;
        ranges.max += 2;
      }
    }
    
    return ranges;
  }
  
  async function analyzeDailyPatterns(stats, ranges) {
    const result = {
      consistent_issue: false,
      severity: 'MEDIUM',
      type: null,
      details: null
    };
  
    if (!stats?.length) return result;
  
    const avgValue = stats.reduce((sum, reading) => sum + reading.avg_value, 0) / stats.length;
    const outOfRangeCount = stats.filter(reading => 
      reading.avg_value < reading.optimal_min || 
      reading.avg_value > reading.optimal_max
    ).length;
    
    const outOfRangePercentage = (outOfRangeCount / stats.length) * 100;
  
    if (outOfRangePercentage > 70) {
      result.consistent_issue = true;
      result.severity = 'HIGH';
      result.type = avgValue < stats[0].optimal_min ? 'LOW' : 'HIGH';
    } else if (outOfRangePercentage > 30) {
      result.consistent_issue = true;
      result.severity = 'MEDIUM';
      result.type = avgValue < stats[0].optimal_min ? 'LOW' : 'HIGH';
    }
  
    return result;
  }
  
  function generateRecommendation(sensor_type, patterns) {
    const recommendations = {
      ext_temp: {
        HIGH: "Consider moving the plant to a cooler location or providing more shade",
        LOW: "Move the plant to a warmer location or away from drafts"
      },
      light: {
        HIGH: "Move the plant to a location with less direct sunlight or add a sheer curtain",
        LOW: "Move the plant closer to a light source or consider supplemental growing lights"
      },
      humidity: {
        HIGH: "Improve air circulation or reduce misting frequency",
        LOW: "Consider using a humidity tray or regular misting"
      },
      soil_moisture_1: {
        HIGH: "Reduce watering frequency and ensure proper drainage",
        LOW: "Increase watering frequency or check for root issues"
      },
      soil_moisture_2: {
        HIGH: "Reduce watering frequency and ensure proper drainage",
        LOW: "Increase watering frequency or check for root issues"
      }
    };
  
    return recommendations[sensor_type]?.[patterns.type] || "Monitor and adjust care as needed";
  }
  
  async function analyzeWateringPattern(wateringHistory) {
    const result = {
      needs_adjustment: false,
      priority: 'MEDIUM',
      recommendation: null
    };
  
    if (!wateringHistory?.length || wateringHistory.length < 2) return result;
  
    const intervals = [];
    for (let i = 1; i < wateringHistory.length; i++) {
      const interval = new Date(wateringHistory[i-1].time_stamp) - 
                      new Date(wateringHistory[i].time_stamp);
      intervals.push(interval / (1000 * 60 * 60));
    }
  
    const avgInterval = intervals.reduce((a, b) => a + b, 0) / intervals.length;
    const stdDev = Math.sqrt(
      intervals.reduce((sq, n) => sq + Math.pow(n - avgInterval, 2), 0) / intervals.length
    );
  
    if (stdDev > avgInterval * 0.5) {
      result.needs_adjustment = true;
      result.priority = 'HIGH';
      result.recommendation = "Watering schedule is irregular. Try to maintain consistent watering intervals.";
    }
  
    return result;
  }
  
  function mapScoreToHealth(score) {
    if (score >= 90) return 'EXCELLENT';
    if (score >= 75) return 'GOOD';
    if (score >= 60) return 'FAIR';
    if (score >= 40) return 'POOR';
    return 'CRITICAL';
  }
  
  async function calculateWateringSchedule(plant_id, plantType) {
    const latestData = await SensorData.readLatestData(plant_id);

    const currentMoisture = (latestData?.[0]?.soil_moisture_1 + latestData?.[0]?.soil_moisture_2) / 2;
    
    const moistureThresholds = {
      'FERN': { min: 60, optimal: 70 },
      'SUCCULENT': { min: 20, optimal: 30 },
      'DEFAULT': { min: 40, optimal: 50 }
    };
    
    const threshold = moistureThresholds[plantType] || moistureThresholds.DEFAULT;

    const moistureLossRate = await estimateMoistureLossRate(plant_id);
    const hoursUntilWatering = Math.max(
      0,
      Math.round((currentMoisture - threshold.min) / moistureLossRate)
    );
  
    return {
      next_in_hours: hoursUntilWatering,
      priority: hoursUntilWatering < 12 ? 'HIGH' : 'MEDIUM',
      details: `Water when soil moisture drops below ${threshold.min}%`
    };
  }
  
  async function estimateMoistureLossRate(plant_id) {
    const endTime = new Date();
    const startTime = new Date(endTime - 24 * 60 * 60 * 1000);
    
    const [readings] = await SensorData.getSensorStats(
      plant_id,
      'soil_moisture_1',
      startTime.toISOString(),
      endTime.toISOString(),
      { removeOutliers: true }
    );
    
    if (!(readings?.total_readings)) return 2;
    
    return Math.max(
      0.5,
      (readings.max_value - readings.min_value) / 24
    );
  }
  
  async function calculateMistingSchedule(plant_id) {
    const latestData = await SensorData.readLatestData(plant_id);

    const currentHumidity = latestData?.[0]?.humidity;
    const optimalHumidity = 70;
    
    const humidityLossRate = 5;
    const hoursUntilMisting = Math.max(
      0,
      Math.round((currentHumidity - optimalHumidity) / humidityLossRate)
    );
  
    return {
      next_in_hours: hoursUntilMisting,
      priority: hoursUntilMisting < 6 ? 'HIGH' : 'MEDIUM',
      details: 'Mist to maintain humidity above 70%'
    };
  }
  
  async function getLatestReadings(plant_id) {
    const latestData = await SensorData.readLatestData(plant_id);
    
    if (!latestData?.[0]) {
      return {
        temperature: null,
        humidity: null,
        light: null,
        soil_moisture: null
      };
    }
  
    return {
      temperature: latestData[0].ext_temp,
      humidity: latestData[0].humidity,
      light: latestData[0].light,
      soil_moisture: (latestData[0].soil_moisture_1 + latestData[0].soil_moisture_2) / 2
    };
  }
  
  async function getRecentActions(plant_id) {
    const endTime = new Date();
    const startTime = new Date(endTime - 7 * 24 * 60 * 60 * 1000);
    
    const [wateringEvents] = await WateringEvent.readDataSeries(
      plant_id,
      startTime.toISOString(),
      endTime.toISOString()
    );
  
    return wateringEvents?.map(event => ({
      type: 'WATERING',
      timestamp: event.time_stamp,
      details: `Watered with ${event.volume}ml for ${event.watering_duration}s`
    })) || [];
  }
  

exports.getSensorHealth = async (req, res) => {
  try {
    const { plant_id, sensor_type } = req.query;
    await validatePlantId(plant_id);
    
    if (!OPTIMAL_RANGES[sensor_type]) {
      throw new Error(`Invalid sensor type: ${sensor_type}`);
    }

    const latestData = await getValidatedSensorData(plant_id);
    const currentValue = latestData[sensor_type];
    
    const currentTime = new Date(latestData.time_stamp);
    const hour = currentTime.getHours();
    const season = getSeason(currentTime);
    
    const adjustedRanges = getAdjustedRanges(OPTIMAL_RANGES[sensor_type], hour, season);
    const health = calculateSensorHealth(currentValue, adjustedRanges);

    const endTime = new Date();
    const startTime = new Date(endTime - 3 * 60 * 60 * 1000);
    const [historicalData] = await SensorData.getSensorStats(
      plant_id,
      sensor_type,
      startTime.toISOString(),
      endTime.toISOString(),
      { removeOutliers: true }
    );

    return res.status(200).send({
      status: health,
      current_value: {
        value: currentValue,
        unit: OPTIMAL_RANGES[sensor_type].unit,
        timestamp: latestData.time_stamp
      },
      optimal_range: {
        min: adjustedRanges.min,
        max: adjustedRanges.max,
        unit: OPTIMAL_RANGES[sensor_type].unit
      },
      historical_context: {
        min: historicalData.min_value,
        max: historicalData.max_value,
        avg: historicalData.avg_value,
        readings: historicalData.total_readings
      },
      sensor_type: sensor_type,
      plant_id: plant_id
    });
  } catch (err) {
    console.error('Error in getSensorHealth:', err);
    return res.status(500).send({ message: err.message });
  }
};

exports.getPlantRecommendations = async (req, res) => {
  try {
    const { plant_id } = req.query;
    await validatePlantId(plant_id);
    
    const endTime = new Date();
    const startTime = new Date(endTime - 7 * 24 * 60 * 60 * 1000);
    
    const recommendations = [];
    
    for (const [sensor, ranges] of Object.entries(OPTIMAL_RANGES)) {
      const [stats] = await SensorData.getSensorStats(
        plant_id,
        sensor,
        startTime.toISOString(),
        endTime.toISOString(),
        { granularity: 'day' }
      );
      
      const dailyPatterns = await analyzeDailyPatterns(stats, ranges);
      
      if (dailyPatterns.consistent_issue) {
        recommendations.push({
          type: sensor.toUpperCase(),
          priority: dailyPatterns.severity,
          message: generateRecommendation(sensor, dailyPatterns)
        });
      }
    }
    
    const [wateringHistory] = await WateringEvent.readDataSeries(
      plant_id,
      startTime.toISOString(),
      endTime.toISOString()
    );
    
    const wateringPattern = await analyzeWateringPattern(wateringHistory);
    if (wateringPattern.needs_adjustment) {
      recommendations.push({
        type: 'WATERING',
        priority: wateringPattern.priority,
        message: wateringPattern.recommendation
      });
    }

    return res.status(200).send({
      plant_id: plant_id,
      timestamp: new Date().toISOString(),
      recommendations: recommendations,
      last_watering: wateringHistory[0] || null
    });
  } catch (err) {
    console.error('Error in getPlantRecommendations:', err);
    return res.status(500).send({ message: err.message });
  }
};

exports.getHealthDiagnostics = async (req, res) => {
  try {
    const { plant_id } = req.query;
    await validatePlantId(plant_id);
    
    // Simplified analysis: only check the latest readings
    const latestReadings = await getLatestReadings(plant_id);
    
    // Simplified health score calculation
    const overallScore = (latestReadings.temperature + latestReadings.humidity + latestReadings.light + latestReadings.soil_moisture) / 4;
    
    // Map score to status
    let overallHealth = mapScoreToHealth(overallScore);

    return res.status(200).send({
      overall_health: overallHealth,
      health_score: Math.round(overallScore),
      latest_readings: latestReadings
    });
  } catch (err) {
    console.error('Error in getHealthDiagnostics:', err);
    return res.status(500).send({ message: err.message });
  }
};

exports.getCareSchedule = async (req, res) => {
  try {
    const { plant_id } = req.query;
    await validatePlantId(plant_id);
    
    // Get plant type and preferences
    const plant = await Plant.readDataById(plant_id);
    const plantType = plant[0].plant_type;
    
    // Calculate watering schedule
    const wateringSchedule = await calculateWateringSchedule(plant_id, plantType);
    
    // Calculate misting schedule if needed
    const mistingSchedule = plantType === 'FERN' ? 
      await calculateMistingSchedule(plant_id) : null;
    
    const nextActions = [];
    if (wateringSchedule) {
      nextActions.push({
        type: 'WATERING',
        due_in_hours: wateringSchedule.next_in_hours,
        priority: wateringSchedule.priority,
        details: wateringSchedule.details
      });
    }
    
    if (mistingSchedule) {
      nextActions.push({
        type: 'MISTING',
        due_in_hours: mistingSchedule.next_in_hours,
        priority: mistingSchedule.priority,
        details: mistingSchedule.details
      });
    }

    return res.status(200).send({
      plant_id: plant_id,
      timestamp: new Date().toISOString(),
      next_actions: nextActions,
      recent_actions: await getRecentActions(plant_id),
      sensor_context: await getLatestReadings(plant_id)
    });
  } catch (err) {
    console.error('Error in getCareSchedule:', err);
    return res.status(500).send({ message: err.message });
  }
};
