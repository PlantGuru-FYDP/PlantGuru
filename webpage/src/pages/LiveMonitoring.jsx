import { useState, useEffect, useRef } from 'react';
import {
  Typography,
  Box,
  Paper,
  Grid,
  IconButton,
  Card,
  CardContent,
  Modal,
  Button,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Switch,
  FormControlLabel,
  Stack,
  Divider
} from '@mui/material';
import {
  Settings as SettingsIcon,
  FilterList as FilterIcon,
  Bluetooth as BluetoothIcon,
  Wifi as WifiIcon,
  Thermostat as ThermostatIcon,
  WbSunny as LightIcon,
  Water as WaterIcon,
  Update as UpdateIcon,
  CloudDownload as DownloadIcon,
  Delete as DeleteIcon
} from '@mui/icons-material';
import { Chart } from 'chart.js/auto';
import 'chartjs-adapter-date-fns';
import { styled } from '@mui/material/styles';

const DEVICE_NAME = "PlantGuru 2";
const BLE_SERVICE = "19b10000-e8f2-537e-4f6c-d104768a1214";
const SENSOR_CHARACTERISTIC = "19b10001-e8f2-537e-4f6c-d104768a1214";
const WIFI_CHARACTERISTIC = "19b10002-e8f2-537e-4f6c-d104768a1214";
const SETTINGS_CHARACTERISTIC = "19b10003-e8f2-537e-4f6c-d104768a1214";
const LOCALSTORAGE_KEY = "sensorData 2";

const modalStyle = {
  position: 'absolute',
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)',
  width: 400,
  bgcolor: 'background.paper',
  boxShadow: 24,
  p: 4,
  borderRadius: 2
};

const SensorCard = styled(Card)(({ theme }) => ({
  height: '100%',
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'space-between',
  '& .MuiCardContent-root': {
    padding: '8px !important',
    '&:last-child': {
      paddingBottom: '8px !important'
    }
  }
}));

export default function LiveMonitoring() {
  const chartRef = useRef(null);
  const chartInstance = useRef(null);
  
  const [sensorData, setSensorData] = useState({
    temp1: '...',
    temp2: '...',
    light: '...',
    soilMoisture1: '...',
    soilMoisture2: '...',
    humidity: '...',
    wifiStatus: 'Disconnected',
    updateTime: '...'
  });

  const [openSettings, setOpenSettings] = useState(false);
  const [openFilter, setOpenFilter] = useState(false);
  const [openBluetooth, setOpenBluetooth] = useState(false);
  const [openWifi, setOpenWifi] = useState(false);
  const [openClearData, setOpenClearData] = useState(false);

  const [settings, setSettings] = useState({
    uploadPeriod: 3000,
    wifiUploadPeriod: 3000,
    sensorRecordingPeriod: 3000,
    sdRecording: false
  });

  const [wifiSettings, setWifiSettings] = useState({
    ssid: 'Data City 263 - 2.4GHz',
    password: ''
  });

  const [filterSettings, setFilterSettings] = useState({
    type: 'all',
    dataPoints: 0,
    timeSpan: {
      seconds: 0,
      minutes: 0,
      hours: 0,
      days: 0
    }
  });

  const [bleState, setBleState] = useState({
    connected: false,
    deviceName: '',
    error: ''
  });

  let bleServer = null;
  let bleServiceFound = null;
  let sensorCharacteristicFound = null;
  let wifiCharacteristicFound = null;
  let settingsCharacteristicFound = null;

  useEffect(() => {
    initChart();
    return () => {
      if (chartInstance.current) {
        chartInstance.current.destroy();
      }
    };
  }, []);

  const initChart = () => {
    const ctx = chartRef.current.getContext("2d");
    
    if (chartInstance.current) {
      chartInstance.current.destroy();
    }

    chartInstance.current = new Chart(ctx, {
      type: "line",
      data: {
        labels: [],
        datasets: [
          {
            label: "Temperature 1",
            yAxisID: "y-degrees",
            data: [],
            borderColor: "rgba(75, 192, 192, 1)",
            borderWidth: 1,
          },
          {
            label: "Temperature 2",
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
            label: "Soil Moisture 1",
            yAxisID: "y-percentage",
            data: [],
            borderColor: "rgba(153, 102, 255, 1)",
            borderWidth: 1,
          },
          {
            label: "Soil Moisture 2",
            yAxisID: "y-percentage",
            data: [],
            borderColor: "rgba(153, 102, 255, 1)",
          },
          {
            label: "Humidity",
            yAxisID: "y-percentage",
            data: [],
            borderColor: "rgba(255, 99, 132, 1)",
            borderWidth: 1,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          x: {
            type: "time",
            time: {
              unit: "minute",
            },
            displayFormats: {
              minute: "HH:mm",
            },
            tooltipFormat: "HH:mm",
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
              text: "Degrees Celsius",
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
              text: "Percentage",
            },
            grid: {
              drawOnChartArea: false,
            },
          },
        },
      },
    });
  };

  const updateChart = () => {
    let storedData = JSON.parse(localStorage.getItem(LOCALSTORAGE_KEY)) || [];

    if (filterSettings.type === "dataPoints" && filterSettings.dataPoints > 0) {
      storedData = storedData.slice(-filterSettings.dataPoints);
    } else if (filterSettings.type === "timeSpan") {
      const totalMs = 
        (filterSettings.timeSpan.seconds + 
        filterSettings.timeSpan.minutes * 60 + 
        filterSettings.timeSpan.hours * 3600 + 
        filterSettings.timeSpan.days * 86400) * 1000;
      
      if (totalMs > 0) {
        const currentTime = Date.now();
        storedData = storedData.filter(data => 
          currentTime - new Date(data.timestamp).getTime() <= totalMs
        );
      }
    }

    if (chartInstance.current) {
      chartInstance.current.data.labels = storedData.map(data => data.timestamp);
      chartInstance.current.data.datasets[0].data = storedData.map(data => data.temp1);
      chartInstance.current.data.datasets[1].data = storedData.map(data => data.temp2);
      chartInstance.current.data.datasets[2].data = storedData.map(data => data.light);
      chartInstance.current.data.datasets[3].data = storedData.map(data => data.soilMoisture1);
      chartInstance.current.data.datasets[4].data = storedData.map(data => data.soilMoisture2);
      chartInstance.current.data.datasets[5].data = storedData.map(data => data.humidity);
      chartInstance.current.update();
    }
  };

  const handleCharacteristicChange = (event) => {
    const value = event.target.value;
    const dataString = new TextDecoder().decode(value);
    let [temp1, temp2, light, soil1, soil2, humidity, wifi] = dataString
      .split(",")
      .map(Number);

    // Convert -1 values to null
    if (temp1 === -1) temp1 = null;
    if (temp2 === -1) temp2 = null;
    if (light === -1) light = null;
    if (soil1 === -1) soil1 = null;
    if (soil2 === -1) soil2 = null;
    if (humidity === -1) humidity = null;

    const newData = {
      temp1,
      temp2,
      light,
      soilMoisture1: soil1,
      soilMoisture2: soil2,
      humidity,
      timestamp: new Date().getTime(),
    };

    // Update local storage
    const storedData = JSON.parse(localStorage.getItem(LOCALSTORAGE_KEY)) || [];
    storedData.push(newData);
    localStorage.setItem(LOCALSTORAGE_KEY, JSON.stringify(storedData));

    // Update state
    setSensorData({
      temp1: temp1 ? `${temp1.toFixed(1)} °C` : 'N/A',
      temp2: temp2 ? `${temp2.toFixed(1)} °C` : 'N/A',
      light: light !== null ? `${light.toFixed(1)} %` : 'N/A',
      soilMoisture1: soil1 !== null ? `${soil1.toFixed(1)} %` : 'N/A',
      soilMoisture2: soil2 !== null ? `${soil2.toFixed(1)} %` : 'N/A',
      humidity: humidity !== null ? `${humidity.toFixed(1)} %` : 'N/A',
      wifiStatus: wifi === 3 ? 'Connected' : 'Disconnected',
      updateTime: new Date().toLocaleTimeString()
    });

    updateChart();
  };

  const connectToDevice = async () => {
    try {
      if (!navigator.bluetooth) {
        throw new Error("Web Bluetooth API is not available in this browser!");
      }

      const device = await navigator.bluetooth.requestDevice({
        filters: [{ name: DEVICE_NAME }],
        optionalServices: [BLE_SERVICE],
      });

      setBleState(prev => ({
        ...prev,
        deviceName: device.name
      }));

      device.addEventListener('gattserverdisconnected', onDisconnected);
      
      const server = await device.gatt.connect();
      bleServer = server;

      const service = await server.getPrimaryService(BLE_SERVICE);
      bleServiceFound = service;

      const [sensorChar, wifiChar, settingsChar] = await Promise.all([
        service.getCharacteristic(SENSOR_CHARACTERISTIC),
        service.getCharacteristic(WIFI_CHARACTERISTIC),
        service.getCharacteristic(SETTINGS_CHARACTERISTIC),
      ]);

      sensorCharacteristicFound = sensorChar;
      wifiCharacteristicFound = wifiChar;
      settingsCharacteristicFound = settingsChar;

      sensorChar.addEventListener('characteristicvaluechanged', handleCharacteristicChange);
      wifiChar.addEventListener('characteristicvaluechanged', handleWiFiStatusChange);
      settingsChar.addEventListener('characteristicvaluechanged', handleSettingsChange);

      await Promise.all([
        sensorChar.startNotifications(),
        wifiChar.startNotifications(),
        settingsChar.startNotifications()
      ]);

      const [sensorValue, wifiValue, settingsValue] = await Promise.all([
        sensorChar.readValue(),
        wifiChar.readValue(),
        settingsChar.readValue()
      ]);

      handleCharacteristicChange({ target: { value: sensorValue } });
      handleWiFiStatusChange({ target: { value: wifiValue } });
      handleSettingsChange({ target: { value: settingsValue } });

      setBleState(prev => ({
        ...prev,
        connected: true,
        error: ''
      }));

    } catch (error) {
      console.error('Error:', error);
      setBleState(prev => ({
        ...prev,
        error: error.message
      }));
    }
  };

  const disconnectDevice = async () => {
    if (bleServer?.connected) {
      try {
        if (sensorCharacteristicFound) {
          await sensorCharacteristicFound.stopNotifications();
        }
        await bleServer.disconnect();
        setBleState({
          connected: false,
          deviceName: '',
          error: ''
        });
      } catch (error) {
        console.error('Error disconnecting:', error);
        setBleState(prev => ({
          ...prev,
          error: error.message
        }));
      }
    }
  };

  const onDisconnected = () => {
    setBleState(prev => ({
      ...prev,
      connected: false
    }));
    connectToDevice();
  };

  const handleWiFiStatusChange = (event) => {
    const status = new TextDecoder().decode(event.target.value);
    setSensorData(prev => ({
      ...prev,
      wifiStatus: status
    }));
  };

  const handleSettingsChange = (event) => {
    const settingsData = new TextDecoder().decode(event.target.value);
    const [uploadPeriod, wifiUploadPeriod, sensorRecordingPeriod, sdRecording] = 
      settingsData.split(",").map(Number);

    setSettings({
      uploadPeriod,
      wifiUploadPeriod,
      sensorRecordingPeriod,
      sdRecording: Boolean(sdRecording)
    });
  };

  const saveSettings = async () => {
    if (settingsCharacteristicFound) {
      try {
        const data = new TextEncoder().encode(
          `${settings.uploadPeriod},${settings.wifiUploadPeriod},${settings.sensorRecordingPeriod},${settings.sdRecording ? 1 : 0}`
        );
        await settingsCharacteristicFound.writeValue(data);
        setOpenSettings(false);
      } catch (error) {
        console.error('Error saving settings:', error);
      }
    }
  };

  const reconnectWifi = async () => {
    if (wifiCharacteristicFound) {
      try {
        const data = new TextEncoder().encode(
          `${wifiSettings.ssid},${wifiSettings.password}`
        );
        await wifiCharacteristicFound.writeValue(data);
        setOpenWifi(false);
      } catch (error) {
        console.error('Error reconnecting WiFi:', error);
      }
    }
  };

  const downloadCSV = () => {
    const storedData = JSON.parse(localStorage.getItem(LOCALSTORAGE_KEY)) || [];
    const csvContent =
      "data:text/csv;charset=utf-8," +
      "Temp Sensor 1,Temp Sensor 2,Light Percentage,Soil Moisture 1,Soil Moisture 2,Humidity,Timestamp\n" +
      storedData
        .map(data =>
          `${data.temp1},${data.temp2},${data.light},${data.soilMoisture1},${data.soilMoisture2},${data.humidity},${data.timestamp}`
        )
        .join("\n");

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `Sensor_Data_${new Date().toLocaleString('default', { month: 'short' })}_${new Date().getDate()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const clearData = () => {
    localStorage.removeItem(LOCALSTORAGE_KEY);
    updateChart();
    setOpenClearData(false);
  };

  return (
    <Box sx={{ p: 1, height: 'calc(100vh - 80px)', display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ mb: 1, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4" component="h1">
          Live Monitoring
        </Typography>
        <Stack direction="row" spacing={1}>
          <IconButton size="small" onClick={() => setOpenFilter(true)}>
            <FilterIcon />
          </IconButton>
          <IconButton size="small" onClick={() => setOpenBluetooth(true)}>
            <BluetoothIcon color={bleState.connected ? "primary" : "default"} />
          </IconButton>
          <IconButton size="small" onClick={() => setOpenWifi(true)}>
            <WifiIcon color={sensorData.wifiStatus === 'Connected' ? "primary" : "default"} />
          </IconButton>
          <IconButton size="small" onClick={() => setOpenSettings(true)}>
            <SettingsIcon />
          </IconButton>
        </Stack>
      </Box>

      <Grid container spacing={1} sx={{ flex: 1, minHeight: 0 }}>
        <Grid item xs={12} md={9}>
          <Paper 
            sx={{ 
              p: 1, 
              height: '100%',
              display: 'flex',
              flexDirection: 'column'
            }}
          >
            <canvas ref={chartRef} style={{ flex: 1 }} />
          </Paper>
        </Grid>
        <Grid item xs={12} md={3} sx={{ height: '100%' }}>
          <Box sx={{ 
            display: 'grid', 
            gridTemplateColumns: { xs: 'repeat(4, 1fr)', md: 'repeat(1, 1fr)' },
            gap: 0.5,
            height: '100%',
            overflowY: 'auto'
          }}>
            <SensorCard>
              <CardContent>
                <Stack spacing={0}>
                  <Stack direction="row" alignItems="center" spacing={0.5}>
                    <ThermostatIcon sx={{ fontSize: '1rem' }} />
                    <Typography variant="caption">Temperature 1</Typography>
                  </Stack>
                  <Typography variant="body1" sx={{ fontWeight: 'medium' }}>{sensorData.temp1}</Typography>
                </Stack>
              </CardContent>
            </SensorCard>
            <SensorCard>
              <CardContent>
                <Stack spacing={0}>
                  <Stack direction="row" alignItems="center" spacing={0.5}>
                    <ThermostatIcon sx={{ fontSize: '1rem' }} />
                    <Typography variant="caption">Temperature 2</Typography>
                  </Stack>
                  <Typography variant="body1" sx={{ fontWeight: 'medium' }}>{sensorData.temp2}</Typography>
                </Stack>
              </CardContent>
            </SensorCard>
            <SensorCard>
              <CardContent>
                <Stack spacing={0}>
                  <Stack direction="row" alignItems="center" spacing={0.5}>
                    <LightIcon sx={{ fontSize: '1rem' }} />
                    <Typography variant="caption">Light</Typography>
                  </Stack>
                  <Typography variant="body1" sx={{ fontWeight: 'medium' }}>{sensorData.light}</Typography>
                </Stack>
              </CardContent>
            </SensorCard>
            <SensorCard>
              <CardContent>
                <Stack spacing={0}>
                  <Stack direction="row" alignItems="center" spacing={0.5}>
                    <WaterIcon sx={{ fontSize: '1rem' }} />
                    <Typography variant="caption">Soil Moisture 1</Typography>
                  </Stack>
                  <Typography variant="body1" sx={{ fontWeight: 'medium' }}>{sensorData.soilMoisture1}</Typography>
                </Stack>
              </CardContent>
            </SensorCard>
            <SensorCard>
              <CardContent>
                <Stack spacing={0}>
                  <Stack direction="row" alignItems="center" spacing={0.5}>
                    <WaterIcon sx={{ fontSize: '1rem' }} />
                    <Typography variant="caption">Soil Moisture 2</Typography>
                  </Stack>
                  <Typography variant="body1" sx={{ fontWeight: 'medium' }}>{sensorData.soilMoisture2}</Typography>
                </Stack>
              </CardContent>
            </SensorCard>
            <SensorCard>
              <CardContent>
                <Stack spacing={0}>
                  <Stack direction="row" alignItems="center" spacing={0.5}>
                    <WaterIcon sx={{ fontSize: '1rem' }} />
                    <Typography variant="caption">Humidity</Typography>
                  </Stack>
                  <Typography variant="body1" sx={{ fontWeight: 'medium' }}>{sensorData.humidity}</Typography>
                </Stack>
              </CardContent>
            </SensorCard>
            <SensorCard>
              <CardContent>
                <Stack spacing={0}>
                  <Stack direction="row" alignItems="center" spacing={0.5}>
                    <WifiIcon sx={{ fontSize: '1rem' }} />
                    <Typography variant="caption">WiFi Status</Typography>
                  </Stack>
                  <Typography variant="body1" sx={{ fontWeight: 'medium' }}>{sensorData.wifiStatus}</Typography>
                </Stack>
              </CardContent>
            </SensorCard>
            <SensorCard>
              <CardContent>
                <Stack spacing={0}>
                  <Stack direction="row" alignItems="center" spacing={0.5}>
                    <UpdateIcon sx={{ fontSize: '1rem' }} />
                    <Typography variant="caption">Last Update</Typography>
                  </Stack>
                  <Typography variant="body1" sx={{ fontWeight: 'medium' }}>{sensorData.updateTime}</Typography>
                </Stack>
              </CardContent>
            </SensorCard>
          </Box>
        </Grid>
      </Grid>

      {/* Settings Modal */}
      <Modal open={openSettings} onClose={() => setOpenSettings(false)}>
        <Box sx={modalStyle}>
          <Typography variant="h6" component="h2" gutterBottom>
            System Settings
          </Typography>
          <Stack spacing={2}>
            <TextField
              label="Bluetooth Upload Period (ms)"
              type="number"
              value={settings.uploadPeriod}
              onChange={(e) => setSettings(prev => ({ ...prev, uploadPeriod: e.target.value }))}
            />
            <TextField
              label="WiFi Upload Period (ms)"
              type="number"
              value={settings.wifiUploadPeriod}
              onChange={(e) => setSettings(prev => ({ ...prev, wifiUploadPeriod: e.target.value }))}
            />
            <TextField
              label="Sensor Recording Period (ms)"
              type="number"
              value={settings.sensorRecordingPeriod}
              onChange={(e) => setSettings(prev => ({ ...prev, sensorRecordingPeriod: e.target.value }))}
            />
            <FormControlLabel
              control={
                <Switch
                  checked={settings.sdRecording}
                  onChange={(e) => setSettings(prev => ({ ...prev, sdRecording: e.target.checked }))}
                />
              }
              label="Enable SD Recording"
            />
            <Button variant="contained" onClick={saveSettings}>
              Save Settings
            </Button>
          </Stack>
        </Box>
      </Modal>

      {/* Filter Modal */}
      <Modal open={openFilter} onClose={() => setOpenFilter(false)}>
        <Box sx={modalStyle}>
          <Typography variant="h6" component="h2" gutterBottom>
            Filter Data
          </Typography>
          <Stack spacing={2}>
            <FormControl fullWidth>
              <InputLabel>Filter Type</InputLabel>
              <Select
                value={filterSettings.type}
                onChange={(e) => setFilterSettings(prev => ({ ...prev, type: e.target.value }))}
              >
                <MenuItem value="all">All</MenuItem>
                <MenuItem value="dataPoints">Custom number of data points</MenuItem>
                <MenuItem value="timeSpan">Custom timespan</MenuItem>
              </Select>
            </FormControl>

            {filterSettings.type === 'dataPoints' && (
              <TextField
                label="Number of data points"
                type="number"
                value={filterSettings.dataPoints}
                onChange={(e) => setFilterSettings(prev => ({ ...prev, dataPoints: e.target.value }))}
              />
            )}

            {filterSettings.type === 'timeSpan' && (
              <Grid container spacing={2}>
                <Grid item xs={3}>
                  <TextField
                    label="Seconds"
                    type="number"
                    value={filterSettings.timeSpan.seconds}
                    onChange={(e) => setFilterSettings(prev => ({
                      ...prev,
                      timeSpan: { ...prev.timeSpan, seconds: e.target.value }
                    }))}
                  />
                </Grid>
                <Grid item xs={3}>
                  <TextField
                    label="Minutes"
                    type="number"
                    value={filterSettings.timeSpan.minutes}
                    onChange={(e) => setFilterSettings(prev => ({
                      ...prev,
                      timeSpan: { ...prev.timeSpan, minutes: e.target.value }
                    }))}
                  />
                </Grid>
                <Grid item xs={3}>
                  <TextField
                    label="Hours"
                    type="number"
                    value={filterSettings.timeSpan.hours}
                    onChange={(e) => setFilterSettings(prev => ({
                      ...prev,
                      timeSpan: { ...prev.timeSpan, hours: e.target.value }
                    }))}
                  />
                </Grid>
                <Grid item xs={3}>
                  <TextField
                    label="Days"
                    type="number"
                    value={filterSettings.timeSpan.days}
                    onChange={(e) => setFilterSettings(prev => ({
                      ...prev,
                      timeSpan: { ...prev.timeSpan, days: e.target.value }
                    }))}
                  />
                </Grid>
              </Grid>
            )}

            <Button variant="contained" onClick={updateChart}>
              Apply Filter
            </Button>
            <Divider />
            <Button
              variant="outlined"
              startIcon={<DownloadIcon />}
              onClick={downloadCSV}
            >
              Download CSV
            </Button>
            <Button
              variant="outlined"
              color="error"
              startIcon={<DeleteIcon />}
              onClick={() => setOpenClearData(true)}
            >
              Clear Data
            </Button>
          </Stack>
        </Box>
      </Modal>

      {/* Bluetooth Modal */}
      <Modal open={openBluetooth} onClose={() => setOpenBluetooth(false)}>
        <Box sx={modalStyle}>
          <Typography variant="h6" component="h2" gutterBottom>
            Bluetooth Connection
          </Typography>
          <Stack spacing={2}>
            <Typography>
              Status: {bleState.connected ? 'Connected' : 'Disconnected'}
              {bleState.deviceName && ` to ${bleState.deviceName}`}
            </Typography>
            {bleState.error && (
              <Typography color="error">
                Error: {bleState.error}
              </Typography>
            )}
            <Button
              variant="contained"
              onClick={bleState.connected ? disconnectDevice : connectToDevice}
            >
              {bleState.connected ? 'Disconnect' : 'Connect to BLE Device'}
            </Button>
          </Stack>
        </Box>
      </Modal>

      {/* WiFi Modal */}
      <Modal open={openWifi} onClose={() => setOpenWifi(false)}>
        <Box sx={modalStyle}>
          <Typography variant="h6" component="h2" gutterBottom>
            WiFi Settings
          </Typography>
          <Stack spacing={2}>
            <TextField
              label="SSID"
              value={wifiSettings.ssid}
              onChange={(e) => setWifiSettings(prev => ({ ...prev, ssid: e.target.value }))}
            />
            <TextField
              label="Password"
              type="password"
              value={wifiSettings.password}
              onChange={(e) => setWifiSettings(prev => ({ ...prev, password: e.target.value }))}
            />
            <Button variant="contained" onClick={reconnectWifi}>
              Reconnect WiFi
            </Button>
          </Stack>
        </Box>
      </Modal>

      {/* Clear Data Confirmation Modal */}
      <Modal open={openClearData} onClose={() => setOpenClearData(false)}>
        <Box sx={modalStyle}>
          <Typography variant="h6" component="h2" gutterBottom>
            Clear Data
          </Typography>
          <Typography gutterBottom>
            Are you sure you want to clear all data?
          </Typography>
          <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
            <Button variant="outlined" onClick={() => setOpenClearData(false)}>
              Cancel
            </Button>
            <Button variant="contained" color="error" onClick={clearData}>
              Clear Data
            </Button>
          </Stack>
        </Box>
      </Modal>
    </Box>
  );
} 