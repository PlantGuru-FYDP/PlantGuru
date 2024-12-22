import { Typography, Box, Paper, Grid, Button } from '@mui/material';
import PresentationIcon from '@mui/icons-material/Slideshow';

export default function Display() {
  const handleOpenPresentation = () => {
    window.open('#/presentation', '_blank');
  };

  return (
    <Box>
      <Typography variant="h3" component="h1" gutterBottom>
        Device Demonstration
      </Typography>
      <Grid container spacing={4}>
        <Grid item xs={12}>
          <Paper 
            sx={{ 
              p: 4, 
              minHeight: '60vh',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'center',
              alignItems: 'center',
              backgroundColor: 'background.paper'
            }}
          >
            <PresentationIcon sx={{ fontSize: 60, color: 'primary.main', mb: 2 }} />
            <Typography variant="h4" gutterBottom>
              Start Presentation
            </Typography>
            <Typography color="text.secondary" paragraph>
              Click the button below to open the interactive presentation in a new window.
            </Typography>
            <Button
              variant="contained"
              color="primary"
              size="large"
              onClick={handleOpenPresentation}
              startIcon={<PresentationIcon />}
            >
              Launch Presentation
            </Button>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
} 