import { Typography, Box, Paper, Grid } from '@mui/material';

export default function Home() {
  return (
    <Box>
      <Typography variant="h2" component="h1" gutterBottom>
        PlantGuru Smart Home Device
      </Typography>
      <Typography variant="h5" component="h2" gutterBottom color="text.secondary" sx={{ maxWidth: '800px', mb: 4 }}>
        The intelligent solution for plant care, combining smart monitoring with predictive technology to ensure your plants thrive.
      </Typography>
      <Grid container spacing={3} sx={{ mt: 2 }}>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              The Challenge
            </Typography>
            <Typography>
              In 2023, sales of potted plants exceeded 1.1 billion dollars in Canada, with more than 61% of Canadians growing plants for personal use. 
              However, many plants die due to improper care, with major causes being over or underwatering. Understanding the specific needs of different 
              plants in varying environments remains a key challenge for plant owners.
            </Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Our Solution
            </Typography>
            <Typography>
              PlantGuru is more than just an app - it's an all-in-one package combining a physical monitor with smart cloud technology. 
              Our system aggregates data from multiple sensors and uses predictive modeling with control theory to determine tailored watering schedules for each plant.
            </Typography>
          </Paper>
        </Grid>
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Key Features
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} md={6}>
                <Typography component="ul">
                  <li>Comprehensive sensor suite (light, soil moisture, and temperature)</li>
                  <li>Cloud-based predictive modeling for personalized care</li>
                  <li>Adaptive watering schedule recommendations</li>
                  <li>Real-time plant health monitoring</li>
                </Typography>
              </Grid>
              <Grid item xs={12} md={6}>
                <Typography component="ul">
                  <li>Detailed plant health reporting</li>
                  <li>User-friendly mobile application</li>
                  <li>Smart notifications and reminders</li>
                  <li>All-in-one integrated solution</li>
                </Typography>
              </Grid>
            </Grid>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
} 