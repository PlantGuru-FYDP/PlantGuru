import { useState, useEffect } from 'react';
import { Box, IconButton, Typography, Paper, Grid } from '@mui/material';
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
    content: (
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
              <PlaceholderImage text="Embedded System" color="primary.main" />
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
              <PlaceholderImage text="Backend Server" color="primary.main" />
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
              <PlaceholderImage text="Mobile App" color="primary.main" />
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
              <PlaceholderImage text="Data Model" color="primary.main" />
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
    content: (
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
            <PlaceholderImage text="Hardware Photo" color="primary.light" />
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
    content: (
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
            <Box sx={{ mb: 4 }}>
              <PlaceholderImage text="Mobile App Screenshots" color="primary.light" />
            </Box>
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
    content: (
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
            <PlaceholderImage text="Data Analysis Visualization" color="primary.light" />
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
    content: (
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
            <Box sx={{ textAlign: 'center', mb: 4 }}>
              <img 
                src="/screenshots/dashboard.png" 
                alt="Dashboard Screenshot"
                style={{ width: '100%', maxWidth: 500, borderRadius: 8, boxShadow: '0 4px 8px rgba(0,0,0,0.1)' }}
              />
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
            <Box sx={{ textAlign: 'center', mb: 4 }}>
              <img 
                src="/screenshots/device-management.png"
                alt="Device Management Screenshot" 
                style={{ width: '100%', maxWidth: 500, borderRadius: 8, boxShadow: '0 4px 8px rgba(0,0,0,0.1)' }}
              />
              <Typography variant="h6" sx={{ mt: 2 }}>
                Device Management
              </Typography>
              <Typography variant="body1">
                Easily configure your PlantGuru devices, set watering schedules, and receive notifications about your plant's needs
              </Typography>
            </Box>
          </motion.div>
        </Grid>
      </Grid>
    ),
  },
  {
    title: 'Demo - Hardware',
    content: (
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
            <Box sx={{ textAlign: 'center', mb: 4 }}>
              <img 
                src="/screenshots/hardware-prototype.png"
                alt="Hardware Prototype"
                style={{ width: '100%', maxWidth: 500, borderRadius: 8, boxShadow: '0 4px 8px rgba(0,0,0,0.1)' }}
              />
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
            <Box sx={{ textAlign: 'center', mb: 4 }}>
              <img 
                src="/screenshots/pcb-design.png"
                alt="PCB Design"
                style={{ width: '100%', maxWidth: 500, borderRadius: 8, boxShadow: '0 4px 8px rgba(0,0,0,0.1)' }}
              />
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
    content: (
      <Box>
        <Typography variant="h3" gutterBottom>
          <SensorsIcon sx={{ fontSize: '2.5rem', verticalAlign: 'middle', mr: 2 }} />
          Real-Time Monitoring
        </Typography>
        <Grid container spacing={4}>
          <Grid item xs={12} md={8}>
            <PlaceholderGraph color="secondary.main" />
          </Grid>
          <Grid item xs={12} md={4}>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.3 }}
            >
              <Typography variant="h6" gutterBottom>
                Live Sensor Data
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                <WaterDropIcon sx={{ color: 'primary.main' }} />
                <Typography variant="body1">
                  Soil Moisture: 65%
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                <DeviceThermostatIcon sx={{ color: 'primary.main' }} />
                <Typography variant="body1">
                  Temperature: 22°C
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                <LightbulbIcon sx={{ color: 'primary.main' }} />
                <Typography variant="body1">
                  Light: 850 lux
                </Typography>
              </Box>
            </motion.div>
          </Grid>
        </Grid>
      </Box>
    ),
  },
   {
    title: 'Results',
    content: (
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
            <Box sx={{ mb: 4 }}>
              <PlaceholderGraph color="success.light" />
              <Typography variant="body2" align="center" sx={{ mt: 1 }}>
                Model Prediction Accuracy
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
            <Box sx={{ mb: 4 }}>
              <PlaceholderGraph color="primary.light" />
              <Typography variant="body2" align="center" sx={{ mt: 1 }}>
                System Performance Metrics
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
            {slides[page].content}
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