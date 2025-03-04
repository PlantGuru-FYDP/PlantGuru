const SensorData = require("../models/sensorModel");
const WateringEvent = require("../models/wateringEventModel");
const Plant = require("../models/plantModel");

const calculateSensorHealth = (value, optimalRanges) => {
  if (value === null || value === undefined) return 'UNKNOWN';
  
  const { min, max, criticalMin, criticalMax } = optimalRanges;
  
  if (value < criticalMin || value > criticalMax) return 'CRITICAL';
  if (value < min) return 'WARNING_LOW';
  if (value > max) return 'WARNING_HIGH';
  return 'GOOD';
};

const OPTIMAL_RANGES = {
  ext_temp: {
    name: 'Ext Temp',
    criticalMin: 0,
    min: 10,
    max: 30,
    criticalMax: 40,
    unit: '°C'
  },
  light: {
    name: 'Light',
    criticalMin: 0,
    min: 0,
    max: 100,
    criticalMax: 100,
    unit: '%'
  },
  humidity: {
    name: 'Humidity',
    criticalMin: 0,
    min: 0,
    max: 70,
    criticalMax: 90,
    unit: '%'
  },
  soil_temp: {
    name: 'Soil Temp',
    criticalMin: 5,
    min: 10,
    max: 30,
    criticalMax: 35,
    unit: '°C'
  },
  soil_moisture_1: {
    name: 'Soil Moisture 1',
    criticalMin: 10,
    min: 30,
    max: 100,
    criticalMax: 100,
    unit: '%'
  },
  soil_moisture_2: {
    name: 'Soil Moisture 2',
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
    
    if (ranges.name === 'Light') {
      const estHour = hour;
      
      if (estHour >= 20 || estHour < 6) { 
        ranges.criticalMin = 0;
        ranges.min = 0;
        ranges.max = 100;
        ranges.criticalMax = 100;
      } else if (estHour < 8 || estHour >= 18) {
        ranges.criticalMin = 0;
        ranges.min = 10;
        ranges.max = 100;
        ranges.criticalMax = 100;
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
      reading.avg_value < ranges.min || 
      reading.avg_value > ranges.max
    ).length;
    
    const outOfRangePercentage = (outOfRangeCount / stats.length) * 100;
  
    if (outOfRangePercentage > 50) {
      result.consistent_issue = true;
      result.severity = 'HIGH';
      result.type = avgValue < ranges.min ? 'LOW' : 'HIGH';
    } else if (outOfRangePercentage > 25) {
      result.consistent_issue = true;
      result.severity = 'MEDIUM';
      result.type = avgValue < ranges.min ? 'LOW' : 'HIGH';
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
    const latestData = await getValidatedSensorData(plant_id);
    
    const currentMoisture = latestData ? 
      (latestData.soil_moisture_1 + latestData.soil_moisture_2) / 2 : null;
    
    const moistureThresholds = {
      'FERN': { min: 60, optimal: 70 },
      'SUCCULENT': { min: 20, optimal: 30 },
      'DEFAULT': { min: 40, optimal: 50 }
    };
    
    const threshold = moistureThresholds[plantType] || moistureThresholds.DEFAULT;
    
    if (currentMoisture === null) {
      return {
        next_in_hours: 0,
        priority: "MEDIUM",
        details: `Water when soil moisture drops below ${threshold.min}%`
      };
    }

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
    const latestData = await getValidatedSensorData(plant_id);
    
    if (!latestData) {
      return {
        temperature: null,
        humidity: null,
        light: null,
        soil_moisture: null
      };
    }
  
    return {
      temperature: latestData.ext_temp,
      humidity: latestData.humidity,
      light: latestData.light,
      soil_moisture: (latestData.soil_moisture_1 + latestData.soil_moisture_2) / 2
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

function generateImmediateRecommendation(sensor_type, health, value) {
    const recommendations = {
      ext_temp: {
        WARNING_HIGH: "Temperature is currently high, consider temporary cooling measures",
        WARNING_LOW: "Temperature is currently low, consider temporary warming measures",
        CRITICAL: "Temperature is at critical levels, immediate action required"
      },
      light: {
        WARNING_HIGH: "Light levels are too intense, consider adding shade",
        WARNING_LOW: "Light levels are too low, consider moving to a brighter location", 
        CRITICAL: "Light levels are at critical levels, immediate action required"
      },
      humidity: {
        WARNING_HIGH: "Humidity is too high, increase ventilation",
        WARNING_LOW: "Humidity is too low, consider misting or using a humidifier",
        CRITICAL: "Humidity is at critical levels, immediate action required"
      },
      soil_moisture_1: {
        WARNING_HIGH: "Soil is too wet, hold off on watering",
        WARNING_LOW: "Soil is getting dry, water soon",
        CRITICAL: "Soil moisture is at critical levels, immediate action required"
      },
      soil_moisture_2: {
        WARNING_HIGH: "Soil is too wet, hold off on watering",
        WARNING_LOW: "Soil is getting dry, water soon", 
        CRITICAL: "Soil moisture is at critical levels, immediate action required"
      }
    };
  
    return recommendations[sensor_type]?.[health] || "Monitor current conditions";
  }
  

const createGoodStatusResponse = (sensor_type, plant_id, timestamp = new Date().toISOString(), value = 0, ranges = null) => {
  const defaultRanges = { min: 0, max: 100, unit: 'unknown' };
  const sensorRanges = ranges || OPTIMAL_RANGES[sensor_type] || defaultRanges;
  
  return {
    immediate_status: {
      status: "GOOD",
      current_value: {
        value,
        unit: sensorRanges.unit,
        timestamp
      },
      optimal_range: {
        min: sensorRanges.min,
        max: sensorRanges.max,
        unit: sensorRanges.unit
      }
    },
    sensor_type,
    plant_id
  };
};

async function getSensorHealthStatuses(latestData) {
  if (!latestData) return null;
  
  return {
    temperature: latestData.ext_temp != null ? 
      calculateSensorHealth(latestData.ext_temp, OPTIMAL_RANGES.ext_temp) : null,
    humidity: latestData.humidity != null ? 
      calculateSensorHealth(latestData.humidity, OPTIMAL_RANGES.humidity) : null,
    soil_moisture: latestData.soil_moisture_1 != null ? 
      calculateSensorHealth(latestData.soil_moisture_1, OPTIMAL_RANGES.soil_moisture_1) : null,
    light: latestData.light != null ? 
      calculateSensorHealth(latestData.light, OPTIMAL_RANGES.light) : null
  };
}

function calculateOverallHealth(sensorStatuses) {
  if (!sensorStatuses) {
    return { health: 'UNKNOWN', score: 0 };
  }

  let totalPoints = 0;
  let totalSensors = 0;

  Object.entries(sensorStatuses).forEach(([sensor, status]) => {
    if (status !== null && status !== 'UNKNOWN') {
      totalSensors++;
      if (status === 'GOOD') {
        totalPoints += 2;
      } else if (status === 'WARNING_HIGH' || status === 'WARNING_LOW') {
        totalPoints += 1;
      }
      // CRITICAL gets 0 points
    }
  });

  if (totalSensors === 0) {
    return { health: 'UNKNOWN', score: 0 };
  }

  const score = Math.round((totalPoints / (totalSensors * 2)) * 100);
  return { 
    health: mapScoreToHealth(score), 
    score 
  };
}

function formatSensorReadings(latestData, sensorStatuses) {
  if (!latestData || !sensorStatuses) {
    return {
      temperature: null,
      humidity: null,
      soil_moisture: null,
      light: null
    };
  }

  return {
    temperature: latestData.ext_temp != null ? {
      value: latestData.ext_temp,
      status: sensorStatuses.temperature
    } : null,
    humidity: latestData.humidity != null ? {
      value: latestData.humidity,
      status: sensorStatuses.humidity
    } : null,
    soil_moisture: latestData.soil_moisture_1 != null ? {
      value: latestData.soil_moisture_1,
      status: sensorStatuses.soil_moisture
    } : null,
    light: latestData.light != null ? {
      value: latestData.light,
      status: sensorStatuses.light
    } : null
  };
}

exports.getSensorHealth = async (req, res) => {
  const { plant_id, sensor_type } = req.query;
  
  try {
    try {
      await validatePlantId(plant_id);
    } catch (err) {
      return res.status(200).send(createGoodStatusResponse(sensor_type, plant_id));
    }

    if (!OPTIMAL_RANGES[sensor_type]) {
      return res.status(200).send(createGoodStatusResponse(sensor_type, plant_id));
    }

    let latestData;
    try {
      latestData = await getValidatedSensorData(plant_id);
    } catch (err) {
      return res.status(200).send(createGoodStatusResponse(sensor_type, plant_id, new Date().toISOString(), 0, OPTIMAL_RANGES[sensor_type]));
    }

    const currentValue = latestData[sensor_type];
    const currentTime = new Date(latestData.time_stamp);
    
    const estDate = new Date(currentTime.toLocaleString('en-US', { timeZone: 'America/New_York' }));
    const utcDate = new Date(currentTime.toLocaleString('en-US', { timeZone: 'UTC' }));
    const offset = utcDate - estDate;
    
    const adjustedEstTime = new Date(currentTime.getTime() - offset);
    const estHour = adjustedEstTime.getHours();
    
    const adjustedRanges = getAdjustedRanges(
      OPTIMAL_RANGES[sensor_type], 
      estHour,
      getSeason(adjustedEstTime)
    );
    
    const currentHealth = calculateSensorHealth(currentValue, adjustedRanges);

    return res.status(200).send({
      immediate_status: {
        status: currentHealth,
        current_value: {
          value: currentValue,
          unit: OPTIMAL_RANGES[sensor_type].unit,
          timestamp: latestData.time_stamp
        },
        optimal_range: {
          min: adjustedRanges.min,
          max: adjustedRanges.max,
          unit: OPTIMAL_RANGES[sensor_type].unit
        }
      },
      sensor_type,
      plant_id
    });
  } catch (err) {
    console.error('Error in getSensorHealth:', err);
    return res.status(200).send(createGoodStatusResponse(sensor_type, plant_id));
  }
};

exports.getPlantRecommendations = async (req, res) => {
  try {
    const { plant_id } = req.query;
    await validatePlantId(plant_id);
    
    const endTime = new Date();
    const startTime = new Date(endTime - 24 * 60 * 60 * 1000);
    
    const currentIssues = [];
    const longTermIssues = [];
    
    const latestData = await getValidatedSensorData(plant_id);
    const currentTime = new Date(latestData.time_stamp);
    const season = getSeason(currentTime);
    const hour = currentTime.getHours();

    for (const [sensor, ranges] of Object.entries(OPTIMAL_RANGES)) {
      const currentValue = latestData[sensor];
      const adjustedRanges = getAdjustedRanges(ranges, hour, season);
      const currentHealth = calculateSensorHealth(currentValue, adjustedRanges);

      if (currentHealth !== 'GOOD') {
        currentIssues.push({
          type: sensor.toUpperCase(),
          status: currentHealth,
          value: currentValue,
          message: generateImmediateRecommendation(sensor, currentHealth, currentValue)
        });
      }

      if (currentHealth !== 'GOOD') {
        const [stats] = await SensorData.getSensorStats(
          plant_id,
          sensor,
          startTime.toISOString(),
          endTime.toISOString(),
          { granularity: 'hour' }
        );
        
        const dailyPatterns = await analyzeDailyPatterns(stats, ranges);
        
        if (dailyPatterns.consistent_issue) {
          longTermIssues.push({
            type: sensor.toUpperCase(),
            priority: dailyPatterns.severity,
            pattern_type: dailyPatterns.type,
            message: generateRecommendation(sensor, dailyPatterns)
          });
        }
      }
    }
    
    const [wateringHistory] = await WateringEvent.readDataSeries(
      plant_id,
      startTime.toISOString(),
      endTime.toISOString()
    );
    
    const wateringPattern = await analyzeWateringPattern(wateringHistory);
    if (wateringPattern.needs_adjustment) {
      longTermIssues.push({
        type: 'WATERING',
        priority: wateringPattern.priority,
        message: wateringPattern.recommendation
      });
    }

    return res.status(200).send({
      plant_id: plant_id,
      timestamp: new Date().toISOString(),
      current_issues: currentIssues,
      long_term_issues: longTermIssues,
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
    
    const latestData = await getValidatedSensorData(plant_id);
    const sensorStatuses = await getSensorHealthStatuses(latestData);
    const { health: overallHealth, score: overallScore } = calculateOverallHealth(sensorStatuses);
    const readingsWithStatus = formatSensorReadings(latestData, sensorStatuses);

    return res.status(200).send({
      overall_health: overallHealth,
      health_score: overallScore,
      latest_readings: readingsWithStatus
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
    
    const [plant] = await Plant.readDataById(plant_id);
    const plantType = plant?.plant_type || 'DEFAULT';
    
    const wateringSchedule = await calculateWateringSchedule(plant_id, plantType);
    const mistingSchedule = plantType === 'FERN' ? 
      await calculateMistingSchedule(plant_id) : null;
    
    const nextActions = [];
    if (wateringSchedule) {
      nextActions.push({
        type: 'WATERING',
        due_in_hours: wateringSchedule.next_in_hours || 0,
        priority: wateringSchedule.priority,
        details: wateringSchedule.details
      });
    }
    
    if (mistingSchedule) {
      nextActions.push({
        type: 'MISTING',
        due_in_hours: mistingSchedule.next_in_hours || 0,
        priority: mistingSchedule.priority,
        details: mistingSchedule.details
      });
    }

    const latestReadings = await getLatestReadings(plant_id);

    return res.status(200).send({
      plant_id: plant_id,
      timestamp: new Date().toISOString(),
      next_actions: nextActions,
      recent_actions: await getRecentActions(plant_id),
      sensor_context: latestReadings || {
        soil_moisture: null,
        temperature: null,
        humidity: null,
        light: null
      }
    });
  } catch (err) {
    console.error('Error in getCareSchedule:', err);
    return res.status(500).send({ message: err.message });
  }
};
