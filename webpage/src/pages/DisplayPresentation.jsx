import { useState, useEffect, useRef } from 'react';
import { Box, IconButton, Typography, Paper, Grid, TextField, Button, Select, MenuItem, FormControl, InputLabel, Modal } from '@mui/material';
import { styled } from '@mui/material/styles';
import { motion, AnimatePresence } from 'framer-motion';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import RestartAltIcon from '@mui/icons-material/RestartAlt';
import WaterDropIcon from '@mui/icons-material/WaterDrop';
import DeviceThermostatIcon from '@mui/icons-material/DeviceThermostat';
import MonitorHeartIcon from '@mui/icons-material/MonitorHeart';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import CloudIcon from '@mui/icons-material/Cloud';
import PhoneIphoneIcon from '@mui/icons-material/PhoneIphone';
import YouTube from 'react-youtube';
import Logo from '../components/Logo';
import ArchitectureIcon from '@mui/icons-material/Architecture';
import MemoryIcon from '@mui/icons-material/Memory';
import SensorsIcon from '@mui/icons-material/Sensors';
import StorageIcon from '@mui/icons-material/Storage';
import TimelineIcon from '@mui/icons-material/Timeline';
import QueryStatsIcon from '@mui/icons-material/QueryStats';
import PrecisionManufacturingIcon from '@mui/icons-material/PrecisionManufacturing';
import SettingsSystemDaydreamIcon from '@mui/icons-material/SettingsSystemDaydream';
import SpeedIcon from '@mui/icons-material/Speed';
import BatteryChargingFullIcon from '@mui/icons-material/BatteryChargingFull';
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive';
import LightbulbIcon from '@mui/icons-material/Lightbulb';
import CloseIcon from '@mui/icons-material/Close';
import { Chart } from 'chart.js/auto';
import 'chartjs-adapter-date-fns';

const FullscreenContainer = styled(Box)(({ theme }) => ({
  minHeight: '100vh',
  backgroundColor: theme.palette.background.default,
  position: 'relative',
  overflow: 'hidden',
}));

const NavigationButton = styled(IconButton)(({ theme }) => ({
  position: 'fixed',
  top: '50%',
  transform: 'translateY(-50%)',
  backgroundColor: theme.palette.primary.main,
  color: theme.palette.common.white,
  zIndex: 1000,
  width: 40,
  height: 40,
  '&:hover': {
    backgroundColor: theme.palette.primary.dark,
  },
  '&.Mui-disabled': {
    backgroundColor: theme.palette.grey[300],
  },
}));

const MotionContainer = styled(motion.div)({
  position: 'absolute',
  width: '100%',
  height: '100%',
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
});

const VideoContainer = styled(Box)(({ theme }) => ({
  width: '100vw',
  height: '100vh',
  position: 'absolute',
  top: 0,
  left: 0,
  right: 0,
  bottom: 0,
  '& iframe': {
    width: '100vw',
    height: '100vh',
    border: 'none',
  },
}));

// Placeholder components for demo purposes
const PlaceholderGraph = ({ color }) => (
  <Box
    sx={{
      width: '100%',
      height: 300,
      backgroundColor: color,
      borderRadius: 2,
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      color: 'white',
    }}
  >
    Graph Placeholder
  </Box>
);

const PlaceholderImage = ({ text, color }) => (
  <Box
    sx={{
      width: '100%',
      height: 200,
      backgroundColor: color,
      borderRadius: 2,
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      color: 'white',
    }}
  >
    {text}
  </Box>
);

const SlideControls = styled(Box)({
  position: 'fixed',
  bottom: 20,
  left: '50%',
  transform: 'translateX(-50%)',
  display: 'flex',
  gap: 1,
  zIndex: 1000,
});

const ImageModal = ({ open, onClose, imageSrc }) => (
  <Modal
    open={open}
    onClose={onClose}
    sx={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      p: 2
    }}
  >
    <Box
      sx={{
        position: 'relative',
        maxWidth: '90vw',
        maxHeight: '90vh',
        outline: 'none',
        bgcolor: 'background.paper',
        borderRadius: 2,
        p: 2,
        boxShadow: 24
      }}
    >
      <IconButton
        onClick={onClose}
        sx={{
          position: 'absolute',
          right: 8,
          top: 8,
          bgcolor: 'background.paper',
          '&:hover': { bgcolor: 'action.hover' }
        }}
      >
        <CloseIcon />
      </IconButton>
      <img
        src={imageSrc}
        alt="Full size view"
        style={{
          maxWidth: '100%',
          maxHeight: 'calc(90vh - 32px)',
          objectFit: 'contain'
        }}
      />
    </Box>
  </Modal>
);

const ClickableImage = ({ src, alt, style, onClick }) => (
  <img
    src={src}
    alt={alt}
    onClick={onClick}
    style={{
      ...style,
      cursor: 'pointer',
      transition: 'transform 0.2s',
      '&:hover': {
        transform: 'scale(1.02)'
      }
    }}
  />
);

const LiveDataDemo = () => {
  const chartRef = useRef(null);
  const chartInstance = useRef(null);
  const [plantId, setPlantId] = useState(11);
  const [customPlantId, setCustomPlantId] = useState("");
  const [sensorData, setSensorData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [bucketMinutes, setBucketMinutes] = useState(30);
  const [timeRange, setTimeRange] = useState({
    start: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString().slice(0, 16), // 24 hours ago
    end: new Date().toISOString().slice(0, 16) // current time
  });

  const plantPresets = {
    7: "Soil",
    9: "Palm",
    10: "Garlic",
    11: "Purple Coleus",
    12: "Spider Plant",
    13: "Onion",
    16: "Watering Position Test (Garlic)",
    custom: "Custom Plant ID"
  };

  const handlePlantChange = (event) => {
    const value = event.target.value;
    if (value === 'custom') {
      setPlantId(parseInt(customPlantId) || 11);
    } else {
      setPlantId(parseInt(value));
      setCustomPlantId("");
    }
  };

  useEffect(() => {
    let timer;
    // Add a small delay to ensure the canvas is ready
    timer = setTimeout(() => {
      if (chartRef.current) {
        initChart();
        fetchData();
      }
    }, 100);
    
    return () => {
      clearTimeout(timer);
      if (chartInstance.current) {
        chartInstance.current.destroy();
        chartInstance.current = null;
      }
    };
  }, []); // Only run on mount and unmount

  const initChart = () => {
    const ctx = chartRef.current.getContext("2d");
    
    if (chartInstance.current) {
      chartInstance.current.destroy();
      chartInstance.current = null;
    }

    chartInstance.current = new Chart(ctx, {
      type: "line",
      data: {
        labels: [],
        datasets: [
          {
            label: "Air Temperature",
            yAxisID: "y-degrees",
            data: [],
            borderColor: "rgba(75, 192, 192, 1)",
            borderWidth: 1,
          },
          {
            label: "Light",
            yAxisID: "y-percentage",
            data: [],
            borderColor: "rgba(255, 206, 86, 1)",
            borderWidth: 1,
          },
          {
            label: "Humidity",
            yAxisID: "y-percentage",
            data: [],
            borderColor: "rgba(153, 102, 255, 1)",
            borderWidth: 1,
          },
          {
            label: "Soil Temperature",
            yAxisID: "y-degrees",
            data: [],
            borderColor: "rgba(255, 99, 132, 1)",
            borderWidth: 1,
          },
          {
            label: "Soil Moisture",
            yAxisID: "y-percentage",
            data: [],
            borderColor: "rgba(54, 162, 235, 1)",
            borderWidth: 1,
          }
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          x: {
            type: "time",
            time: {
              displayFormats: {
                millisecond: 'HH:mm:ss.SSS',
                second: 'HH:mm:ss',
                minute: 'HH:mm',
                hour: 'HH:mm',
                day: 'MMM d',
                week: 'MMM d',
                month: 'MMM yyyy',
                quarter: 'MMM yyyy',
                year: 'yyyy'
              },
              tooltipFormat: 'MMM d, yyyy HH:mm',
              unit: 'day',
              stepSize: 1,
              minUnit: 'hour'
            },
            title: {
              display: true,
              text: "Time"
            },
            ticks: {
              maxRotation: 0,
              autoSkip: true,
              autoSkipPadding: 20
            }
          },
          "y-degrees": {
            type: "linear",
            position: "left",
            beginAtZero: true,
            ticks: {
              callback: function (value) {
                return value + "°C";
              },
            },
            title: {
              display: true,
              text: "Temperature (°C)",
            },
          },
          "y-percentage": {
            type: "linear",
            position: "right",
            beginAtZero: true,
            ticks: {
              callback: function (value) {
                return value + "%";
              },
            },
            title: {
              display: true,
              text: "Percentage (%)",
            },
            grid: {
              drawOnChartArea: false,
            },
          },
        },
      },
    });
  };

  const smoothData = (data, bucketMinutes = 30) => {
    const buckets = {};
    
    // Group data into time buckets
    data.forEach(point => {
      const timestamp = new Date(point.time_stamp);
      // Round to nearest bucket
      timestamp.setMinutes(Math.floor(timestamp.getMinutes() / bucketMinutes) * bucketMinutes);
      timestamp.setSeconds(0);
      timestamp.setMilliseconds(0);
      
      const key = timestamp.toISOString();
      if (!buckets[key]) {
        buckets[key] = {
          count: 0,
          ext_temp: { sum: 0, count: 0 },
          light: { sum: 0, count: 0 },
          humidity: { sum: 0, count: 0 },
          soil_temp: { sum: 0, count: 0 },
          soil_moisture_1: { sum: 0, count: 0 },
          soil_moisture_2: { sum: 0, count: 0 }
        };
      }
      
      // Only add non-null values to the sums
      if (point.ext_temp !== null) {
        buckets[key].ext_temp.sum += point.ext_temp;
        buckets[key].ext_temp.count++;
      }
      if (point.light !== null) {
        buckets[key].light.sum += point.light;
        buckets[key].light.count++;
      }
      if (point.humidity !== null) {
        buckets[key].humidity.sum += point.humidity;
        buckets[key].humidity.count++;
      }
      if (point.soil_temp !== null) {
        buckets[key].soil_temp.sum += point.soil_temp;
        buckets[key].soil_temp.count++;
      }
      if (point.soil_moisture_1 !== null) {
        buckets[key].soil_moisture_1.sum += point.soil_moisture_1;
        buckets[key].soil_moisture_1.count++;
      }
      if (point.soil_moisture_2 !== null) {
        buckets[key].soil_moisture_2.sum += point.soil_moisture_2;
        buckets[key].soil_moisture_2.count++;
      }
      buckets[key].count++;
    });
    
    // Calculate averages for each bucket, handling null values
    return Object.entries(buckets).map(([time, values]) => ({
      time_stamp: time,
      ext_temp: values.ext_temp.count > 0 ? values.ext_temp.sum / values.ext_temp.count : null,
      light: values.light.count > 0 ? values.light.sum / values.light.count : null,
      humidity: values.humidity.count > 0 ? values.humidity.sum / values.humidity.count : null,
      soil_temp: values.soil_temp.count > 0 ? values.soil_temp.sum / values.soil_temp.count : null,
      soil_moisture_1: values.soil_moisture_1.count > 0 ? values.soil_moisture_1.sum / values.soil_moisture_1.count : null,
      soil_moisture_2: values.soil_moisture_2.count > 0 ? values.soil_moisture_2.sum / values.soil_moisture_2.count : null
    })).sort((a, b) => new Date(a.time_stamp) - new Date(b.time_stamp));
  };

  const fetchData = async () => {
    setLoading(true);
    try {
      const startTime = new Date(timeRange.start);
      const endTime = new Date(timeRange.end);
      
      const response = await fetch(
        `http://52.14.140.110:3000/api/sensorReadSeries?plant_id=${plantId}&time_stamp1=${startTime.toISOString()}&time_stamp2=${endTime.toISOString()}`,
        {
          // Add these headers to handle CORS
          headers: {
            'Access-Control-Allow-Origin': '*',
            'Content-Type': 'application/json',
          },
          // Allow insecure requests
          mode: 'cors',
        }
      );
      
      const data = await response.json();
      const smoothedData = smoothData(data.result, bucketMinutes);
      setSensorData(data.result);
      
      if (chartInstance.current) {
        chartInstance.current.data.labels = smoothedData.map(d => d.time_stamp);
        chartInstance.current.data.datasets[0].data = smoothedData.map(d => ({ x: d.time_stamp, y: d.ext_temp }));
        chartInstance.current.data.datasets[1].data = smoothedData.map(d => ({ x: d.time_stamp, y: d.light }));
        chartInstance.current.data.datasets[2].data = smoothedData.map(d => ({ x: d.time_stamp, y: d.humidity }));
        chartInstance.current.data.datasets[3].data = smoothedData.map(d => ({ x: d.time_stamp, y: d.soil_temp }));
        chartInstance.current.data.datasets[4].data = smoothedData.map(d => ({ x: d.time_stamp, y: d.soil_moisture_1 }));
        chartInstance.current.update();
      }
    } catch (error) {
      console.error('Error fetching sensor data:', error);
    }
    setLoading(false);
  };

  const handleRefresh = () => {
    fetchData();
  };

  return (
    <Box sx={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Typography variant="h3" gutterBottom>
        <SensorsIcon sx={{ fontSize: '2.5rem', verticalAlign: 'middle', mr: 2 }} />
        Real-Time Monitoring
      </Typography>
      <Grid container spacing={4} sx={{ flexGrow: 1, minHeight: 0 }}>
        <Grid item xs={12} md={8} sx={{ height: '100%' }}>
          <Paper sx={{ 
            p: 2, 
            height: '100%',
            display: 'flex',
            flexDirection: 'column'
          }}>
            <Box sx={{ flexGrow: 1, position: 'relative', minHeight: 0 }}>
              <canvas ref={chartRef} style={{ position: 'absolute', width: '100%', height: '100%' }} />
            </Box>
          </Paper>
        </Grid>
        <Grid item xs={12} md={4} sx={{ height: '100%' }}>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.3 }}
            style={{ height: '100%' }}
          >
            <Paper sx={{ p: 2, height: '100%' }}>
              <Typography variant="h6" gutterBottom>
                Chart Controls
              </Typography>
              <Box sx={{ mb: 3 }}>
                <FormControl fullWidth sx={{ mb: 2 }}>
                  <InputLabel>Plant Selection</InputLabel>
                  <Select
                    value={customPlantId ? 'custom' : plantId.toString()}
                    onChange={handlePlantChange}
                    label="Plant Selection"
                  >
                    {Object.entries(plantPresets).map(([id, name]) => 
                      id === 'custom' ? (
                        <MenuItem key={id} value={id}>
                          <Box sx={{ width: '100%' }}>
                            <Typography>{name}</Typography>
                            {id === 'custom' && (
                              <TextField
                                size="small"
                                type="number"
                                placeholder="Enter Plant ID"
                                value={customPlantId}
                                onChange={(e) => {
                                  setCustomPlantId(e.target.value);
                                  if (e.target.value) {
                                    setPlantId(parseInt(e.target.value));
                                  }
                                }}
                                onClick={(e) => e.stopPropagation()}
                                sx={{ mt: 1, width: '100%' }}
                              />
                            )}
                          </Box>
                        </MenuItem>
                      ) : (
                        <MenuItem key={id} value={id}>
                          {name} (ID: {id})
                        </MenuItem>
                      )
                    )}
                  </Select>
                </FormControl>
                
                <Typography variant="subtitle2" gutterBottom>
                  Time Range
                </Typography>
                <TextField
                  label="Start Time"
                  type="datetime-local"
                  value={timeRange.start}
                  onChange={(e) => setTimeRange(prev => ({ ...prev, start: e.target.value }))}
                  size="small"
                  fullWidth
                  sx={{ mb: 1 }}
                />
                <TextField
                  label="End Time"
                  type="datetime-local"
                  value={timeRange.end}
                  onChange={(e) => setTimeRange(prev => ({ ...prev, end: e.target.value }))}
                  size="small"
                  fullWidth
                  sx={{ mb: 2 }}
                />
                
                <Typography variant="subtitle2" gutterBottom>
                  Data Smoothing (minutes)
                </Typography>
                <TextField
                  type="number"
                  value={bucketMinutes}
                  onChange={(e) => setBucketMinutes(Number(e.target.value))}
                  size="small"
                  fullWidth
                  sx={{ mb: 2 }}
                  inputProps={{ min: 1, max: 120 }}
                />
                
                <Button
                  variant="contained"
                  onClick={handleRefresh}
                  disabled={loading}
                  fullWidth
                >
                  Update Chart
                </Button>
              </Box>
              
              {sensorData && sensorData[0] && (
                <Box>
                  <Typography variant="subtitle2" gutterBottom>
                    Latest Readings
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                    <WaterDropIcon sx={{ color: 'primary.main' }} />
                    <Typography variant="body1">
                      Soil Moisture: {sensorData[0].soil_moisture_1 !== null ? sensorData[0].soil_moisture_1.toFixed(1) + '%' : 'N/A'}
                    </Typography>
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                    <DeviceThermostatIcon sx={{ color: 'primary.main' }} />
                    <Typography variant="body1">
                      Air Temperature: {sensorData[0].ext_temp !== null ? sensorData[0].ext_temp.toFixed(1) + '°C' : 'N/A'}
                    </Typography>
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                    <LightbulbIcon sx={{ color: 'primary.main' }} />
                    <Typography variant="body1">
                      Light: {sensorData[0].light !== null ? sensorData[0].light.toFixed(1) + '%' : 'N/A'}
                    </Typography>
                  </Box>
                </Box>
              )}
            </Paper>
          </motion.div>
        </Grid>
      </Grid>
    </Box>
  );
};

const MobileScreenshotCarousel = ({ handleImageClick }) => {
  const [currentImage, setCurrentImage] = useState(0);

  const mobileScreenshots = [
    {
      src: "/PlantGuru/presentation_images/screenshots/app_data.png",
      alt: "Data View",
      title: "Data Dashboard"
    },
    {
      src: "/PlantGuru/presentation_images/screenshots/app_all_plants.png",
      alt: "All Plants View",
      title: "Plant Management"
    },
    {
      src: "/PlantGuru/presentation_images/screenshots/app_plant.png",
      alt: "Plant Details View",
      title: "Plant Details"
    }
  ];

  const handlePrevImage = () => {
    setCurrentImage((prev) => (prev === 0 ? mobileScreenshots.length - 1 : prev - 1));
  };

  const handleNextImage = () => {
    setCurrentImage((prev) => (prev === mobileScreenshots.length - 1 ? 0 : prev + 1));
  };

  return (
    <Box sx={{ 
      mb: 4,
      height: '60vh',
      width: '100%',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      position: 'relative'
    }}>
      <Box sx={{
        flex: 1,
        width: '100%',
        position: 'relative',
        overflow: 'hidden',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center'
      }}>
        <IconButton 
          sx={{ 
            position: 'absolute', 
            left: 16,
            top: '50%', 
            transform: 'translateY(-50%)',
            backgroundColor: 'rgba(255, 255, 255, 0.8)',
            '&:hover': {
              backgroundColor: 'rgba(255, 255, 255, 0.9)'
            },
            zIndex: 2
          }}
          onClick={handlePrevImage}
        >
          <ArrowBackIcon />
        </IconButton>

        <Box sx={{
          position: 'relative',
          height: '100%',
          width: '100%',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center'
        }}>
          <AnimatePresence initial={false}>
            <motion.div
              key={currentImage}
              style={{
                position: 'absolute',
                height: '100%',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center'
              }}
              initial={{ x: 300, opacity: 0 }}
              animate={{ x: 0, opacity: 1 }}
              exit={{ x: -300, opacity: 0 }}
              transition={{
                x: { type: "spring", stiffness: 300, damping: 30 },
                opacity: { duration: 0.2 }
              }}
            >
              <ClickableImage
                src={mobileScreenshots[currentImage].src}
                alt={mobileScreenshots[currentImage].alt}
                onClick={() => handleImageClick(mobileScreenshots[currentImage].src)}
                style={{ 
                  height: '100%',
                  width: 'auto',
                  maxWidth: '100%',
                  objectFit: 'contain',
                  borderRadius: 8,
                  boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                }}
              />
            </motion.div>
          </AnimatePresence>
        </Box>

        <IconButton 
          sx={{ 
            position: 'absolute', 
            right: 16,
            top: '50%', 
            transform: 'translateY(-50%)',
            backgroundColor: 'rgba(255, 255, 255, 0.8)',
            '&:hover': {
              backgroundColor: 'rgba(255, 255, 255, 0.9)'
            },
            zIndex: 2
          }}
          onClick={handleNextImage}
        >
          <ArrowForwardIcon />
        </IconButton>
      </Box>

      <Typography variant="h6" align="center" sx={{ mt: 2 }}>
        {mobileScreenshots[currentImage].title}
      </Typography>

      <Box sx={{ 
        display: 'flex', 
        justifyContent: 'center', 
        gap: 1,
        mt: 2
      }}>
        {mobileScreenshots.map((_, index) => (
          <Box
            key={index}
            onClick={() => setCurrentImage(index)}
            sx={{
              width: 8,
              height: 8,
              borderRadius: '50%',
              backgroundColor: currentImage === index ? 'primary.main' : 'grey.300',
              cursor: 'pointer',
              transition: 'background-color 0.3s'
            }}
          />
        ))}
      </Box>
    </Box>
  );
};

const slides = [
  {
    title: 'PlantGuru Smart Device',
    content: (
      <Box sx={{ textAlign: 'center' }}>
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 4 }}>
          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ duration: 0.5 }}
          >
            <Logo sx={{ width: 200, height: 200, color: 'primary.dark' }} />
          </motion.div>
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.3 }}
          >
            <Typography variant="h1" sx={{ fontSize: '15rem', color: 'primary.dark' }}>
              PlantGuru
            </Typography>
          </motion.div>
        </Box>
        <motion.div
          initial={{ y: 20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.5 }}
        >
          <Typography variant="h5" color="text.secondary" sx={{ maxWidth: 600, mx: 'auto', fontSize: '1.8rem' }}>
            The Intelligent Plant Care Solution
          </Typography>
        </motion.div>
        <motion.div
          initial={{ y: 20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.7 }}
        >
          <Typography variant="h6" color="text.secondary" sx={{ mt: 4, fontSize: '1.4rem' }}>
            Group 2025.02
          </Typography>
          <Typography variant="body1" sx={{ mt: 1, fontSize: '1.2rem' }}>
            Randeep Bedi • Joshua Hamburger • Priyanshu Meshram • Taksh Parmar • Kevin Tan
          </Typography>
        </motion.div>
      </Box>
    ),
  },
  {
    title: 'The Challenge & Solution',
    content: (
      <Grid container spacing={4}>
        <Grid item xs={12}>
          <Typography variant="h3" gutterBottom>
            Why PlantGuru?
          </Typography>
        </Grid>
        <Grid item xs={12} md={6}>
          <motion.div
            initial={{ x: -20, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ delay: 0.3 }}
          >
            <Typography variant="h6" gutterBottom color="error.main" sx={{ fontSize: '1.8rem' }}>
              The Problem
            </Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
              <WaterDropIcon sx={{ fontSize: '2rem', color: 'error.main' }} />
              <Typography variant="body1" sx={{ fontSize: '1.5rem' }}>
                Plants often die due to improper watering
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
              <DeviceThermostatIcon sx={{ fontSize: '2rem', color: 'error.main' }} />
              <Typography variant="body1" sx={{ fontSize: '1.5rem' }}>
                Each plant has unique environmental needs
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
              <MonitorHeartIcon sx={{ fontSize: '2rem', color: 'error.main' }} />
              <Typography variant="body1" sx={{ fontSize: '1.5rem' }}>
                Lack of consistent monitoring leads to plant death
              </Typography>
            </Box>
          </motion.div>
        </Grid>
        <Grid item xs={12} md={6}>
          <motion.div
            initial={{ x: 20, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ delay: 0.5 }}
          >
            <Typography variant="h6" gutterBottom color="primary.dark" sx={{ fontSize: '1.8rem' }}>
              Our Solution
            </Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
              <SmartToyIcon sx={{ fontSize: '2rem', color: 'success.main' }} />
              <Typography variant="body1" sx={{ fontSize: '1.5rem' }}>
                Predictive watering schedule using control theory
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
              <CloudIcon sx={{ fontSize: '2rem', color: 'success.main' }} />
              <Typography variant="body1" sx={{ fontSize: '1.5rem' }}>
                Adaptive smart home device with cloud integration
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
              <PhoneIphoneIcon sx={{ fontSize: '2rem', color: 'success.main' }} />
              <Typography variant="body1" sx={{ fontSize: '1.5rem' }}>
                Mobile app for detailed plant health monitoring
              </Typography>
            </Box>
          </motion.div>
        </Grid>
      </Grid>
    ),
  },
  {
    title: 'Objectives',
    content: (
      <Grid container spacing={4}>
        <Grid item xs={12}>
          <Typography variant="h3" gutterBottom>
            Project Objectives
          </Typography>
        </Grid>
        <Grid item xs={12} md={4}>
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.3 }}
          >
            <Box sx={{ textAlign: 'center', mb: 4 }}>
              <SmartToyIcon sx={{ fontSize: '4rem', color: 'primary.main', mb: 2 }} />
              <Typography variant="h6" gutterBottom>
                Smart Monitoring
              </Typography>
              <Typography variant="body1">
                Create an intelligent device that monitors plant health through multiple environmental sensors and stores data in the cloud
              </Typography>
            </Box>
          </motion.div>
        </Grid>
        <Grid item xs={12} md={4}>
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.5 }}
          >
            <Box sx={{ textAlign: 'center', mb: 4 }}>
              <PhoneIphoneIcon sx={{ fontSize: '4rem', color: 'primary.main', mb: 2 }} />
              <Typography variant="h6" gutterBottom>
                Mobile Integration
              </Typography>
              <Typography variant="body1">
                Develop an intuitive mobile app for real-time monitoring, plant management, and notifications
              </Typography>
            </Box>
          </motion.div>
        </Grid>
        <Grid item xs={12} md={4}>
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.7 }}
          >
            <Box sx={{ textAlign: 'center', mb: 4 }}>
              <CloudIcon sx={{ fontSize: '4rem', color: 'primary.main', mb: 2 }} />
              <Typography variant="h6" gutterBottom>
                Predictive Analytics
              </Typography>
              <Typography variant="body1">
                Implement a model which can predict soil moisture levels with precision into the future, and can create a watering schedule for optimal health
              </Typography>
            </Box>
          </motion.div>
        </Grid>
      </Grid>
    ),
  },
  {
    title: 'System Overview',
    content: (props) => (
      <Grid container spacing={4}>
        <Grid item xs={12}>
          <Typography variant="h3" gutterBottom>
            System Architecture
          </Typography>
        </Grid>
        <Grid container spacing={3}>
          <Grid item xs={12} md={3}>
            <motion.div
              initial={{ y: 20, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              transition={{ delay: 0.3 }}
            >
              <Box sx={{ textAlign: 'center', mb: 2 }}>
                <MemoryIcon sx={{ fontSize: '3rem', color: 'primary.main' }} />
              </Box>
              <Box sx={{ 
                height: '35vh',
                display: 'flex',
                flexDirection: 'column'
              }}>
                <Box sx={{
                  flex: 1,
                  position: 'relative',
                  overflow: 'hidden'
                }}>
                  <ClickableImage 
                    src="/PlantGuru/presentation_images/enclosure.png"
                    alt="Embedded System"
                    onClick={() => props.handleImageClick("/PlantGuru/presentation_images/enclosure.png")}
                    style={{ 
                      width: '100%',
                      height: '100%',
                      objectFit: 'contain',
                      borderRadius: 8,
                      boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                    }}
                  />
                </Box>
              </Box>
              <Box sx={{ p: 2, borderLeft: 3, borderColor: 'primary.main', mt: 2 }}>
                <Typography variant="h6" gutterBottom>
                  Embedded System
                </Typography>
                <Typography variant="body1">
                  Multi-sensor monitoring platform with Wi-Fi and Bluetooth connectivity
                </Typography>
              </Box>
            </motion.div>
          </Grid>
          <Grid item xs={12} md={3}>
            <motion.div
              initial={{ y: 20, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              transition={{ delay: 0.5 }}
            >
              <Box sx={{ textAlign: 'center', mb: 2 }}>
                <StorageIcon sx={{ fontSize: '3rem', color: 'primary.main' }} />
              </Box>
              <Box sx={{ 
                height: '35vh',
                display: 'flex',
                flexDirection: 'column'
              }}>
                <Box sx={{
                  flex: 1,
                  position: 'relative',
                  overflow: 'hidden'
                }}>
                  <ClickableImage 
                    src="/PlantGuru/presentation_images/data_model.png"
                    alt="Backend Server"
                    onClick={() => props.handleImageClick("/PlantGuru/presentation_images/data_model.png")}
                    style={{ 
                      width: '100%',
                      height: '100%',
                      objectFit: 'contain',
                      borderRadius: 8,
                      boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                    }}
                  />
                </Box>
              </Box>
              <Box sx={{ p: 2, borderLeft: 3, borderColor: 'primary.main', mt: 2 }}>
                <Typography variant="h6" gutterBottom>
                  Backend Server
                </Typography>
                <Typography variant="body1">
                  Cloud infrastructure for data storage and processing
                </Typography>
              </Box>
            </motion.div>
          </Grid>
          <Grid item xs={12} md={3}>
            <motion.div
              initial={{ y: 20, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              transition={{ delay: 0.7 }}
            >
              <Box sx={{ textAlign: 'center', mb: 2 }}>
                <PhoneIphoneIcon sx={{ fontSize: '3rem', color: 'primary.main' }} />
              </Box>
              <Box sx={{ 
                height: '35vh',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center'
              }}>
                <Box sx={{
                  flex: 1,
                  position: 'relative',
                  overflow: 'hidden',
                  display: 'flex',
                  justifyContent: 'center'
                }}>
                  <ClickableImage 
                    src="/PlantGuru/presentation_images/screenshots/app_data.png"
                    alt="Mobile App Screenshots"
                    onClick={() => props.handleImageClick("/PlantGuru/presentation_images/screenshots/app_data.png")}
                    style={{ 
                      height: '100%',
                      width: 'auto',
                      maxWidth: '100%',
                      objectFit: 'contain',
                      borderRadius: 8,
                      boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                    }}
                  />
                </Box>
              </Box>
              <Box sx={{ p: 2, borderLeft: 3, borderColor: 'primary.main', mt: 2 }}>
                <Typography variant="h6" gutterBottom>
                  Mobile App
                </Typography>
                <Typography variant="body1">
                  User interface for monitoring and receiving notifications
                </Typography>
              </Box>
            </motion.div>
          </Grid>
          <Grid item xs={12} md={3}>
            <motion.div
              initial={{ y: 20, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              transition={{ delay: 0.9 }}
            >
              <Box sx={{ textAlign: 'center', mb: 2 }}>
                <QueryStatsIcon sx={{ fontSize: '3rem', color: 'primary.main' }} />
              </Box>
              <Box sx={{ 
                height: '35vh',
                display: 'flex',
                flexDirection: 'column'
              }}>
                <Box sx={{
                  flex: 1,
                  position: 'relative',
                  overflow: 'hidden'
                }}>
                  <ClickableImage 
                    src="/PlantGuru/presentation_images/moisture_animation_purple_coleus_event3_hybrid-ezgif.com-video-to-gif-converter.gif"
                    alt="Data Model"
                    onClick={() => props.handleImageClick("/PlantGuru/presentation_images/moisture_animation_purple_coleus_event3_hybrid-ezgif.com-video-to-gif-converter.gif")}
                    style={{ 
                      width: '100%',
                      height: '100%',
                      objectFit: 'contain',
                      borderRadius: 8,
                      boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                    }}
                  />
                </Box>
              </Box>
              <Box sx={{ p: 2, borderLeft: 3, borderColor: 'primary.main', mt: 2 }}>
                <Typography variant="h6" gutterBottom>
                  Data Model
                </Typography>
                <Typography variant="body1">
                  AI-powered predictive modeling for optimal plant care
                </Typography>
              </Box>
            </motion.div>
          </Grid>
        </Grid>
      </Grid>
    ),
  },
  {
    title: 'Implementation - Hardware',
    content: (props) => (
      <Grid container spacing={4}>
        <Grid item xs={12}>
          <Typography variant="h3" gutterBottom>
            <PrecisionManufacturingIcon sx={{ fontSize: '2.5rem', verticalAlign: 'middle', mr: 2 }} />
            Hardware Implementation
          </Typography>
        </Grid>
        <Grid item xs={12} md={6}>
          <motion.div
            initial={{ x: -20, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ delay: 0.3 }}
          >
            <Box sx={{ 
              height: '50vh',
              display: 'flex',
              flexDirection: 'column'
            }}>
              <Box sx={{
                flex: 1,
                position: 'relative',
                overflow: 'hidden'
              }}>
                <ClickableImage 
                  src="/PlantGuru/presentation_images/enclosure.png"
                  alt="Hardware Photo"
                  onClick={() => props.handleImageClick("/PlantGuru/presentation_images/enclosure.png")}
                  style={{ 
                    width: '100%',
                    height: '100%',
                    objectFit: 'contain',
                    borderRadius: 8,
                    boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                  }}
                />
              </Box>
            </Box>
          </motion.div>
        </Grid>
        <Grid item xs={12} md={6}>
          <motion.div
            initial={{ x: 20, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ delay: 0.5 }}
          >
            <Typography variant="h6" gutterBottom>
              Key Components
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography variant="body1">• ESP32 Microcontroller with Wi-Fi/BLE</Typography>
              <Typography variant="body1">• Soil Moisture Sensor</Typography>
              <Typography variant="body1">• Temperature & Humidity Sensors</Typography>
              <Typography variant="body1">• Light Intensity Sensor</Typography>
              <Typography variant="body1">• Custom PCB Design</Typography>
            </Box>
          </motion.div>
        </Grid>
      </Grid>
    ),
  },
  {
    title: 'Implementation - Software',
    content: (props) => (
      <Grid container spacing={4}>
        <Grid item xs={12}>
          <Typography variant="h3" gutterBottom>
            <PhoneIphoneIcon sx={{ fontSize: '2.5rem', verticalAlign: 'middle', mr: 2 }} />
            Software Architecture
          </Typography>
        </Grid>
        <Grid item xs={12} md={6}>
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.3 }}
          >
            <MobileScreenshotCarousel handleImageClick={props.handleImageClick} />
          </motion.div>
        </Grid>
        <Grid item xs={12} md={6}>
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.5 }}
          >
            <Typography variant="h6" gutterBottom>
              Features
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography variant="body1">• Real-time Sensor Monitoring</Typography>
              <Typography variant="body1">• Push Notifications</Typography>
              <Typography variant="body1">• Historical Data Visualization</Typography>
              <Typography variant="body1">• Device Management</Typography>
              <Typography variant="body1">• Watering Schedule Insights</Typography>
            </Box>
          </motion.div>
        </Grid>
      </Grid>
    ),
  },
  {
    title: 'Implementation - Data',
    content: (props) => (
      <Grid container spacing={4}>
        <Grid item xs={12}>
          <Typography variant="h3" gutterBottom>
            <TimelineIcon sx={{ fontSize: '2.5rem', verticalAlign: 'middle', mr: 2 }} />
            Data Modeling
          </Typography>
        </Grid>
        <Grid item xs={12} md={8}>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.3 }}
          >
            <Box sx={{ 
              height: '50vh',
              display: 'flex',
              flexDirection: 'column'
            }}>
              <Box sx={{
                flex: 1,
                position: 'relative',
                overflow: 'hidden'
              }}>
                <ClickableImage 
                  src="/PlantGuru/presentation_images/data_model.png"
                  alt="Model Prediction Accuracy"
                  onClick={() => props.handleImageClick("/PlantGuru/presentation_images/data_model.png")}
                  style={{ 
                    width: '100%',
                    height: '100%',
                    objectFit: 'contain',
                    borderRadius: 8,
                    boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                  }}
                />
              </Box>
            </Box>
          </motion.div>
        </Grid>
        <Grid item xs={12} md={4}>
          <motion.div
            initial={{ x: 20, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ delay: 0.5 }}
          >
            <Typography variant="h6" gutterBottom>
              Analysis Techniques
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography variant="body1">• Time Series Analysis</Typography>
              <Typography variant="body1">• Control Theory Modeling</Typography>
              <Typography variant="body1">• Feature Engineering</Typography>
              <Typography variant="body1">• Machine Learning Models</Typography>
              <Typography variant="body1">• Performance Validation</Typography>
            </Box>
          </motion.div>
        </Grid>
      </Grid>
    ),
  },
  {
    title: 'Demo - Software',
    content: (props) => (
      <Grid container spacing={4}>
        <Grid item xs={12}>
          <Typography variant="h3" gutterBottom>
            Application & Data Model Demo
          </Typography>
        </Grid>
        <Grid item xs={12} md={6}>
          <motion.div
            initial={{ x: -20, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ delay: 0.3 }}
          >
            <Box sx={{ 
              textAlign: 'center', 
              mb: 4,
              height: '60vh',
              display: 'flex',
              flexDirection: 'column'
            }}>
              <Box sx={{
                flex: 1,
                position: 'relative',
                overflow: 'hidden'
              }}>
                <ClickableImage 
                  src="/PlantGuru/presentation_images/screenshots/app_data.png" 
                  alt="App Data View"
                  onClick={() => props.handleImageClick("/PlantGuru/presentation_images/screenshots/app_data.png")}
                  style={{ 
                    height: '100%',
                    width: 'auto',
                    maxWidth: '100%',
                    objectFit: 'contain',
                    borderRadius: 8,
                    boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                  }}
                />
              </Box>
              <Typography variant="h6" sx={{ mt: 2 }}>
                Real-time Dashboard
              </Typography>
              <Typography variant="body1">
                Monitor live sensor data, view historical trends, and get insights about your plant's health all in one place
              </Typography>
            </Box>
          </motion.div>
        </Grid>
        <Grid item xs={12} md={6}>
          <motion.div
            initial={{ x: 20, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ delay: 0.5 }}
          >
            <Box sx={{ 
              textAlign: 'center', 
              mb: 4,
              height: '60vh',
              display: 'flex',
              flexDirection: 'column'
            }}>
              <Box sx={{
                flex: 1,
                position: 'relative',
                overflow: 'hidden'
              }}>
                <ClickableImage 
                  src="/PlantGuru/presentation_images/screenshots/app_all_plants.png"
                  alt="All Plants View" 
                  onClick={() => props.handleImageClick("/PlantGuru/presentation_images/screenshots/app_all_plants.png")}
                  style={{ 
                    height: '100%',
                    width: 'auto',
                    maxWidth: '100%',
                    objectFit: 'contain',
                    borderRadius: 8,
                    boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                  }}
                />
              </Box>
              <Typography variant="h6" sx={{ mt: 2 }}>
                Plant Management
              </Typography>
              <Typography variant="body1">
                Easily manage all your plants and monitor their health status at a glance
              </Typography>
            </Box>
          </motion.div>
        </Grid>
      </Grid>
    ),
  },
  {
    title: 'Demo - Hardware',
    content: (props) => (
      <Grid container spacing={4}>
        <Grid item xs={12}>
          <Typography variant="h3" gutterBottom>
            Hardware System Demo
          </Typography>
        </Grid>
        <Grid item xs={12} md={6}>
          <motion.div
            initial={{ x: -20, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ delay: 0.3 }}
          >
            <Box sx={{ 
              textAlign: 'center', 
              mb: 4,
              height: '50vh',
              display: 'flex',
              flexDirection: 'column'
            }}>
              <Box sx={{
                flex: 1,
                position: 'relative',
                overflow: 'hidden'
              }}>
                <ClickableImage 
                  src="/PlantGuru/presentation_images/enclosure.png"
                  alt="Hardware Prototype"
                  onClick={() => props.handleImageClick("/PlantGuru/presentation_images/enclosure.png")}
                  style={{ 
                    width: '100%',
                    height: '100%',
                    objectFit: 'contain',
                    borderRadius: 8,
                    boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                  }}
                />
              </Box>
              <Typography variant="h6" sx={{ mt: 2 }}>
                Working Prototype
              </Typography>
              <Typography variant="body1">
                Our compact and efficient hardware solution featuring multiple environmental sensors and automated watering control
              </Typography>
            </Box>
          </motion.div>
        </Grid>
        <Grid item xs={12} md={6}>
          <motion.div
            initial={{ x: 20, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ delay: 0.5 }}
          >
            <Box sx={{ 
              textAlign: 'center', 
              mb: 4,
              height: '50vh',
              display: 'flex',
              flexDirection: 'column'
            }}>
              <Box sx={{
                flex: 1,
                position: 'relative',
                overflow: 'hidden'
              }}>
                <ClickableImage 
                  src="/PlantGuru/presentation_images/pcb.png"
                  alt="PCB Design"
                  onClick={() => props.handleImageClick("/PlantGuru/presentation_images/pcb.png")}
                  style={{ 
                    width: '100%',
                    height: '100%',
                    objectFit: 'contain',
                    borderRadius: 8,
                    boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                  }}
                />
              </Box>
              <Typography variant="h6" sx={{ mt: 2 }}>
                Custom PCB Design
              </Typography>
              <Typography variant="body1">
                Purpose-built circuit board integrating ESP32, environmental sensors, and power management for reliable operation
              </Typography>
            </Box>
          </motion.div>
        </Grid>
        <Grid item xs={12}>
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.7 }}
          >
            <Box sx={{ mt: 2 }}>
              <Typography variant="h6" gutterBottom>
                Key Features
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} md={4}>
                  <Typography variant="body1">• Real-time soil moisture monitoring</Typography>
                  <Typography variant="body1">• Temperature and humidity sensing</Typography>
                </Grid>
                <Grid item xs={12} md={4}>
                  <Typography variant="body1">• Automated watering system</Typography>
                  <Typography variant="body1">• Light intensity measurement</Typography>
                </Grid>
                <Grid item xs={12} md={4}>
                  <Typography variant="body1">• Wi-Fi & Bluetooth connectivity</Typography>
                  <Typography variant="body1">• Long battery life with efficient power management</Typography>
                </Grid>
              </Grid>
            </Box>
          </motion.div>
        </Grid>
      </Grid>
    ),
  },
  {
    title: 'Product Video',
    content: (
      <Box sx={{ 
        height: '100vh',
        width: '100%',   
        position: 'relative',
        mx: 'auto',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center'
      }}>
        <Box sx={{
          flex: 1,
          position: 'relative',
          width: '100%',
          '& iframe': {
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            borderRadius: 2,
          }
        }}>
          <YouTube
            videoId="dQw4w9WgXcQ"
            opts={{
              width: '100%',
              height: '100%',
              playerVars: {
                autoplay: 1,
                mute: 1,
                controls: 1,
                modestbranding: 1,
                rel: 0,
                showinfo: 0,
                fs: 1,
              },
            }}
          />
        </Box>
      </Box>
    ),
  },
  {
    title: 'Live Data Demo',
    content: <LiveDataDemo />
  },
   {
    title: 'Results',
    content: (props) => (
      <Grid container spacing={4}>
        <Grid item xs={12}>
          <Typography variant="h3" gutterBottom>
            <QueryStatsIcon sx={{ fontSize: '2.5rem', verticalAlign: 'middle', mr: 2 }} />
            Project Results
          </Typography>
        </Grid>
        <Grid item xs={12} md={6}>
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.3 }}
          >
            <Box sx={{ mb: 2 }}>
              <Box sx={{ 
                height: '40vh',
                display: 'flex',
                flexDirection: 'column'
              }}>
                <Box sx={{
                  flex: 1,
                  position: 'relative',
                  overflow: 'hidden'
                }}>
                  <ClickableImage 
                    src="/PlantGuru/presentation_images/data_model.png"
                    alt="Model Prediction Accuracy"
                    onClick={() => props.handleImageClick("/PlantGuru/presentation_images/data_model.png")}
                    style={{ 
                      width: '100%',
                      height: '100%',
                      objectFit: 'contain',
                      borderRadius: 8,
                      boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                    }}
                  />
                </Box>
              </Box>
              <Typography variant="body2" align="center" sx={{ mt: 1 }}>
                Model Prediction Results
              </Typography>
            </Box>
          </motion.div>
        </Grid>
        <Grid item xs={12} md={6}>
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.5 }}
          >
            <Box sx={{ mb: 2 }}>
              <Box sx={{ 
                height: '40vh',
                display: 'flex',
                flexDirection: 'column'
              }}>
                <Box sx={{
                  flex: 1,
                  position: 'relative',
                  overflow: 'hidden'
                }}>
                  <ClickableImage 
                    src="/PlantGuru/presentation_images/moisture_animation_purple_coleus_event3_hybrid-ezgif.com-video-to-gif-converter.gif"
                    alt="System Performance"
                    onClick={() => props.handleImageClick("/PlantGuru/presentation_images/moisture_animation_purple_coleus_event3_hybrid-ezgif.com-video-to-gif-converter.gif")}
                    style={{ 
                      width: '100%',
                      height: '100%',
                      objectFit: 'contain',
                      borderRadius: 8,
                      boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                    }}
                  />
                </Box>
              </Box>
              <Typography variant="body2" align="center" sx={{ mt: 1 }}>
                Real-time Soil Moisture Prediction
              </Typography>
            </Box>
          </motion.div>
        </Grid>
        <Grid item xs={12}>
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.7 }}
          >
            <Typography variant="h6" gutterBottom>
              Key Achievements
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} md={4}>
                <Box sx={{ p: 2, borderLeft: 3, borderColor: 'success.main' }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                    <SpeedIcon sx={{ color: 'success.main' }} />
                    <Typography variant="body1">
                      <strong>Model Accuracy:</strong> Achieved 98.5% accuracy in predicting optimal watering times
                    </Typography>
                  </Box>
                </Box>
              </Grid>
              <Grid item xs={12} md={4}>
                <Box sx={{ p: 2, borderLeft: 3, borderColor: 'success.main' }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                    <BatteryChargingFullIcon sx={{ color: 'success.main' }} />
                    <Typography variant="body1">
                      <strong>Battery Life:</strong> 30+ days of continuous operation on a single charge
                    </Typography>
                  </Box>
                </Box>
              </Grid>
              <Grid item xs={12} md={4}>
                <Box sx={{ p: 2, borderLeft: 3, borderColor: 'success.main' }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                    <NotificationsActiveIcon sx={{ color: 'success.main' }} />
                    <Typography variant="body1">
                      <strong>Response Time:</strong> Real-time notifications within 5 seconds of critical events
                    </Typography>
                  </Box>
                </Box>
              </Grid>
            </Grid>
          </motion.div>
        </Grid>
      </Grid>
    ),
  },
];

export default function DisplayPresentation() {
  const [[page, direction], setPage] = useState([0, 0]);
  const [currentImage, setCurrentImage] = useState(0);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedImage, setSelectedImage] = useState(null);

  const handleImageClick = (imageSrc) => {
    setSelectedImage(imageSrc);
    setModalOpen(true);
  };

  const paginate = (newDirection) => {
    const newPage = page + newDirection;
    if (newPage >= 0 && newPage < slides.length) {
      setPage([newPage, newDirection]);
    }
  };

  const handleReset = () => {
    setPage([0, -1]); // Reset to first slide with backward animation
  };

  const handleKeyDown = (event) => {
    if ((event.key === 'ArrowRight' || event.key === ' ') && page < slides.length - 1) {
      paginate(1);
    } else if (event.key === 'ArrowLeft' && page > 0) {
      paginate(-1);
    }
  };

  useEffect(() => {
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [page]);

  const slideVariants = {
    enter: (direction) => ({
      x: direction > 0 ? 1000 : -1000,
      opacity: 0
    }),
    center: {
      zIndex: 1,
      x: 0,
      opacity: 1
    },
    exit: (direction) => ({
      zIndex: 0,
      x: direction < 0 ? 1000 : -1000,
      opacity: 0
    })
  };

  return (
    <FullscreenContainer>
      <ImageModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        imageSrc={selectedImage}
      />

      <AnimatePresence initial={false} custom={direction}>
        <MotionContainer
          key={page}
          custom={direction}
          variants={slideVariants}
          initial="enter"
          animate="center"
          exit="exit"
          transition={{
            x: { type: "spring", stiffness: 300, damping: 30 },
            opacity: { duration: 0.2 }
          }}
        >
          <Paper 
            elevation={0}
            sx={{ 
              width: '100%',
              height: '100vh',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'center',
              alignItems: 'center',
              padding: { xs: 8, sm: 12 },
              paddingX: { xs: 6, sm: 8 },
              position: 'relative',
              backgroundColor: 'transparent',
              maxWidth: '100%',
              '& > *': {
                maxWidth: 'calc(100% - 80px)',
                width: '100%'
              },
              '& .MuiTypography-body1': {
                fontSize: '1.5rem !important'
              },
              '& .MuiTypography-h6': {
                fontSize: '1.8rem !important'
              }
            }}
          >
            {typeof slides[page].content === 'function' 
              ? slides[page].content({ handleImageClick }) 
              : slides[page].content}
          </Paper>
        </MotionContainer>
      </AnimatePresence>

      <NavigationButton
        onClick={() => paginate(-1)}
        sx={{ left: 20 }}
        size="large"
        disabled={page === 0}
      >
        <ArrowBackIcon />
      </NavigationButton>

      {page === slides.length - 1 ? (
        <NavigationButton
          onClick={handleReset}
          sx={{ right: 20 }}
          size="large"
        >
          <RestartAltIcon />
        </NavigationButton>
      ) : (
        <NavigationButton
          onClick={() => paginate(1)}
          sx={{ right: 20 }}
          size="large"
          disabled={page === slides.length - 1}
        >
          <ArrowForwardIcon />
        </NavigationButton>
      )}

      <SlideControls>
        {slides.map((_, index) => (
          <Box
            key={index}
            sx={{
              width: 8,
              height: 8,
              borderRadius: '50%',
              backgroundColor: index === page ? 'primary.dark' : 'grey.300',
              cursor: 'pointer',
            }}
            onClick={() => setPage([index, index > page ? 1 : -1])}
          />
        ))}
      </SlideControls>
    </FullscreenContainer>
  );
} 