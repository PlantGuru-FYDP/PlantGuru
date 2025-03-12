import { useState, useEffect } from 'react';
import { 
  Box, 
  Typography, 
  Paper, 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  Alert,
  Snackbar,
  CircularProgress,
  TextField
} from '@mui/material';

const ModelChange = () => {
  const [plants, setPlants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [updating, setUpdating] = useState(false);
  const [customAgeValues, setCustomAgeValues] = useState({});

  const modelOptions = [
    { value: 1, label: "Default (ARMAX Model)", description: "Current model using ARMAX prediction" },
    { value: 2, label: "Linear Regression", description: "Simple linear regression on last 10 data points" },
    { value: 3, label: "Exponential Decay", description: "Exponential decay to 50% of dry threshold" },
    { value: 'custom', label: "Custom Model", description: "Enter a custom age value" }
  ];

  useEffect(() => {
    fetchPlants();
  }, []);

  const fetchPlants = async () => {
    setLoading(true);
    try {
      // Fetch all plants from the backend
      const response = await fetch('http://52.14.140.110:3000/api/allPlants');
      if (!response.ok) {
        throw new Error('Failed to fetch plants');
      }
      const data = await response.json();
      
      // Initialize custom age values and ensure all plants have an age value
      const initialCustomValues = {};
      const processedData = data.map(plant => ({
        ...plant,
        age: plant.age || 1, // Default to 1 if age is null
        useCustomAge: false
      }));
      
      processedData.forEach(plant => {
        initialCustomValues[plant.plant_id] = plant.age;
      });
      setCustomAgeValues(initialCustomValues);
      
      setPlants(processedData);
    } catch (err) {
      console.error('Error fetching plants:', err);
      setError('Failed to load plants. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const handleModelChange = (plantId, newValue) => {
    if (newValue === 'custom') {
      // Keep the current age value when switching to custom
      setPlants(plants.map(plant => 
        plant.plant_id === plantId ? { ...plant, age: customAgeValues[plantId], useCustomAge: true } : plant
      ));
    } else {
      // Update the plant's age and custom age value
      setPlants(plants.map(plant => 
        plant.plant_id === plantId ? { ...plant, age: newValue, useCustomAge: false } : plant
      ));
      setCustomAgeValues({
        ...customAgeValues,
        [plantId]: newValue
      });
    }
  };

  const handleCustomAgeChange = (plantId, newValue) => {
    const numericValue = parseInt(newValue) || 1;
    setCustomAgeValues({
      ...customAgeValues,
      [plantId]: numericValue
    });
    
    // If the plant is currently using a custom age, update it
    setPlants(plants.map(plant => 
      plant.plant_id === plantId && plant.useCustomAge ? { ...plant, age: numericValue } : plant
    ));
  };

  const updatePlantModel = async (plantId, age) => {
    setUpdating(true);
    try {
      const response = await fetch('http://52.14.140.110:3000/api/updatePlant', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          plant_id: plantId,
          age: age
        }),
      });

      if (!response.ok) {
        throw new Error('Failed to update plant model');
      }

      setSuccess(`Successfully updated model for plant ID: ${plantId}`);
    } catch (err) {
      console.error('Error updating plant model:', err);
      setError(`Failed to update model for plant ID: ${plantId}`);
    } finally {
      setUpdating(false);
    }
  };

  const updateAllPlants = async () => {
    setUpdating(true);
    try {
      // Create an array of promises for each plant update
      const updatePromises = plants.map(plant => 
        fetch('http://52.14.140.110:3000/api/updatePlant', {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            plant_id: plant.plant_id,
            age: plant.age
          }),
        })
      );

      // Wait for all updates to complete
      await Promise.all(updatePromises);
      setSuccess('Successfully updated all plants');
    } catch (err) {
      console.error('Error updating all plants:', err);
      setError('Failed to update all plants');
    } finally {
      setUpdating(false);
    }
  };

  const resetAllToDefault = async () => {
    setUpdating(true);
    try {
      // Update local state
      const updatedPlants = plants.map(plant => ({
        ...plant,
        age: 1,
        useCustomAge: false
      }));
      setPlants(updatedPlants);
      
      // Create an array of promises for each plant update
      const updatePromises = updatedPlants.map(plant => 
        fetch('http://52.14.140.110:3000/api/updatePlant', {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            plant_id: plant.plant_id,
            age: 1
          }),
        })
      );

      // Wait for all updates to complete
      await Promise.all(updatePromises);
      setSuccess('Successfully reset all plants to default model');
    } catch (err) {
      console.error('Error resetting plants:', err);
      setError('Failed to reset plants');
    } finally {
      setUpdating(false);
    }
  };

  return (
    <Box sx={{ p: 4 }}>
      <Typography variant="h4" gutterBottom>
        Model Selection Interface
      </Typography>
      <Typography variant="body1" paragraph>
        This page allows you to change the projection model used for each plant. The model is determined by the plant's age value.
      </Typography>

      <Box sx={{ mb: 3 }}>
        <Typography variant="h6" gutterBottom>Available Models:</Typography>
        {modelOptions.filter(model => model.value !== 'custom').map(model => (
          <Box key={model.value} sx={{ mb: 1 }}>
            <Typography variant="subtitle1">
              <strong>{model.label}</strong> (Age = {model.value})
            </Typography>
            <Typography variant="body2">{model.description}</Typography>
          </Box>
        ))}
        <Box sx={{ mb: 1 }}>
          <Typography variant="subtitle1">
            <strong>Custom Model</strong>
          </Typography>
          <Typography variant="body2">Enter a custom age value to experiment with different models</Typography>
        </Box>
      </Box>

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <>
          <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
            <Button 
              variant="contained" 
              color="primary" 
              onClick={updateAllPlants} 
              disabled={updating}
            >
              {updating ? <CircularProgress size={24} color="inherit" /> : 'Update All Plants'}
            </Button>
            <Button 
              variant="outlined" 
              color="secondary" 
              onClick={resetAllToDefault} 
              disabled={updating}
            >
              Reset All to Default
            </Button>
          </Box>

          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Plant ID</TableCell>
                  <TableCell>Plant Name</TableCell>
                  <TableCell>Current Model (Age)</TableCell>
                  <TableCell>Custom Age</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {plants.map((plant) => (
                  <TableRow key={plant.plant_id}>
                    <TableCell>{plant.plant_id}</TableCell>
                    <TableCell>{plant.plant_name}</TableCell>
                    <TableCell>
                      <FormControl fullWidth size="small">
                        <InputLabel>Model</InputLabel>
                        <Select
                          value={plant.useCustomAge ? 'custom' : (plant.age || 1)}
                          onChange={(e) => handleModelChange(plant.plant_id, e.target.value)}
                          label="Model"
                        >
                          {modelOptions.map(option => (
                            <MenuItem key={option.value} value={option.value}>
                              {option.label}
                            </MenuItem>
                          ))}
                        </Select>
                      </FormControl>
                    </TableCell>
                    <TableCell>
                      <TextField
                        type="number"
                        size="small"
                        value={customAgeValues[plant.plant_id] || 1}
                        onChange={(e) => handleCustomAgeChange(plant.plant_id, e.target.value)}
                        disabled={!plant.useCustomAge}
                        inputProps={{ min: 1 }}
                        sx={{ width: '100px' }}
                      />
                    </TableCell>
                    <TableCell>
                      <Button 
                        variant="outlined" 
                        onClick={() => updatePlantModel(plant.plant_id, plant.age)}
                        disabled={updating}
                      >
                        Update
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      )}

      <Snackbar 
        open={!!error} 
        autoHideDuration={6000} 
        onClose={() => setError(null)}
      >
        <Alert severity="error" onClose={() => setError(null)}>
          {error}
        </Alert>
      </Snackbar>

      <Snackbar 
        open={!!success} 
        autoHideDuration={6000} 
        onClose={() => setSuccess(null)}
      >
        <Alert severity="success" onClose={() => setSuccess(null)}>
          {success}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default ModelChange; 