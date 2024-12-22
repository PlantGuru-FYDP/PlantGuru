import { Typography, Box, Paper, Grid, Button, Stack } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import GitHubIcon from '@mui/icons-material/GitHub';
import ArticleIcon from '@mui/icons-material/Article';
import PresentationIcon from '@mui/icons-material/Slideshow';
import MonitorIcon from '@mui/icons-material/MonitorHeart';
import DownloadIcon from '@mui/icons-material/Download';

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
        <Grid item xs={12}>
          <Paper sx={{ p: 3, mt: 3 }}>
            <Typography variant="h6" gutterBottom>
              Quick Links
            </Typography>
            <Stack direction="row" spacing={2} sx={{ flexWrap: 'wrap', gap: 2 }}>
              <Button
                component={RouterLink}
                to="/docs"
                variant="contained"
                startIcon={<ArticleIcon />}
              >
                Documentation
              </Button>
              <Button
                component={RouterLink}
                to="/presentation"
                variant="contained"
                startIcon={<PresentationIcon />}
              >
                Capstone Presentation
              </Button>
              <Button
                component="a"
                href="https://github.com/SYSC4907-PlantGuru/PlantGuru"
                target="_blank"
                rel="noopener noreferrer"
                variant="contained"
                startIcon={<GitHubIcon />}
              >
                GitHub Repository
              </Button>
              <Button
                component={RouterLink}
                to="/live"
                variant="contained"
                startIcon={<MonitorIcon />}
              >
                Live Monitoring
              </Button>
              <Box sx={{ pl: 0 }}>
                <Button 
                  variant="contained" 
                  startIcon={<DownloadIcon />}
                  href="/PlantGuru.apk"
                  download
                >
                  Download PlantGuru APK
                </Button>
                <Typography variant="caption" display="block" sx={{ mt: 1 }}>
                  Download and install the latest version of PlantGuru for Android
                </Typography>
              </Box>
            </Stack>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
} 