import { Typography, Box, Paper, Grid, Button, Alert, AlertTitle } from '@mui/material';
import PresentationIcon from '@mui/icons-material/Slideshow';
import SecurityIcon from '@mui/icons-material/Security';

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
            
            <Alert 
              severity="warning" 
              icon={<SecurityIcon />}
              sx={{ 
                mb: 4, 
                width: '100%', 
                maxWidth: 600,
                '& .MuiAlert-message': {
                  width: '100%'
                }
              }}
            >
              <AlertTitle>Important: Browser Security Settings</AlertTitle>
              <Typography variant="body1" paragraph>
                The real-time monitoring feature requires a change to your browser security settings:
              </Typography>
              <Box sx={{ pl: 2 }}>
                <ol style={{ margin: 0, paddingLeft: '20px' }}>
                  <Typography variant="body1" component="li" sx={{ mb: 1 }}>
                    Once the presentation opens, click the lock icon (🔒) in your browser's address bar
                  </Typography>
                  <Typography variant="body1" component="li" sx={{ mb: 1 }}>
                    Select "Site Settings"
                  </Typography>
                  <Typography variant="body1" component="li" sx={{ mb: 1 }}>
                    Scroll to find "Insecure content" and change it to "Allow"
                  </Typography>
                  <Typography variant="body1" component="li">
                    Refresh the page to see live sensor data
                  </Typography>
                </ol>
              </Box>
            </Alert>

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