import { useState, useEffect } from 'react';
import { Box, IconButton, Typography, Paper, Grid } from '@mui/material';
import { styled } from '@mui/material/styles';
import { motion, AnimatePresence } from 'framer-motion';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import RestartAltIcon from '@mui/icons-material/RestartAlt';
import YouTube from 'react-youtube';
import Logo from '../components/Logo';

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
        <motion.div
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          transition={{ duration: 0.5 }}
        >
          <Logo sx={{ width: 120, height: 120, color: 'primary.dark', mb: 3 }} />
        </motion.div>
        <motion.div
          initial={{ y: 20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.3 }}
        >
          <Typography variant="h2" gutterBottom>
            Welcome to PlantGuru
          </Typography>
        </motion.div>
        <motion.div
          initial={{ y: 20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.5 }}
        >
          <Typography variant="h5" color="text.secondary" sx={{ maxWidth: 600, mx: 'auto', fontSize: '1.8rem' }}>
            The Intelligent Plant Care Solution
          </Typography>
        </motion.div>
      </Box>
    ),
  },
  {
    title: 'The Challenge',
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
            <Typography variant="h6" gutterBottom color="primary.dark" sx={{ fontSize: '1.8rem' }}>
              The Market
            </Typography>
            <Typography variant="body1" paragraph sx={{ fontSize: '1.5rem' }}>
              • $1.1+ billion in potted plant sales in Canada (2023)
            </Typography>
            <Typography variant="body1" paragraph sx={{ fontSize: '1.5rem' }}>
              • 61% of Canadians grow plants for personal use
            </Typography>
          </motion.div>
        </Grid>
        <Grid item xs={12} md={6}>
          <motion.div
            initial={{ x: 20, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ delay: 0.4 }}
          >
            <Typography variant="h6" gutterBottom color="primary.dark" sx={{ fontSize: '1.8rem' }}>
              The Problem
            </Typography>
            <Typography variant="body1" paragraph sx={{ fontSize: '1.5rem' }}>
              • Plants often die due to improper care
            </Typography>
            <Typography variant="body1" paragraph sx={{ fontSize: '1.5rem' }}>
              • Over/underwatering is a major cause
            </Typography>
            <Typography variant="body1" paragraph sx={{ fontSize: '1.5rem' }}>
              • Each plant has unique needs in different environments
            </Typography>
          </motion.div>
        </Grid>
      </Grid>
    ),
  },
  {
    title: 'Our Solution',
    content: (
      <Grid container spacing={4}>
        <Grid item xs={12}>
          <Typography variant="h3" gutterBottom>
            Smart Plant Monitoring
          </Typography>
        </Grid>
        <Grid item xs={12} md={6}>
          <PlaceholderGraph color="primary.main" />
        </Grid>
        <Grid item xs={12} md={6}>
          <Typography variant="h6" gutterBottom>
            Comprehensive Monitoring
          </Typography>
          <motion.div
            initial={{ x: -20, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ delay: 0.3 }}
          >
            <Typography variant="body1" paragraph>
              • Soil Moisture Sensing
            </Typography>
          </motion.div>
          <motion.div
            initial={{ x: -20, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ delay: 0.4 }}
          >
            <Typography variant="body1" paragraph>
              • Temperature Monitoring
            </Typography>
          </motion.div>
          <motion.div
            initial={{ x: -20, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ delay: 0.5 }}
          >
            <Typography variant="body1" paragraph>
              • Light Exposure Tracking
            </Typography>
          </motion.div>
        </Grid>
      </Grid>
    ),
  },
  {
    title: 'Smart Features',
    content: (
      <Grid container spacing={4}>
        <Grid item xs={12}>
          <Typography variant="h3" gutterBottom>
            Intelligent Care System
          </Typography>
        </Grid>
        <Grid item xs={12} md={4}>
          <motion.div whileHover={{ scale: 1.05 }}>
            <PlaceholderImage text="Predictive Modeling" color="custom.waterBlue" />
            <Typography variant="body1" align="center" sx={{ mt: 2 }}>
              Cloud-based analytics for personalized care
            </Typography>
          </motion.div>
        </Grid>
        <Grid item xs={12} md={4}>
          <motion.div whileHover={{ scale: 1.05 }}>
            <PlaceholderImage text="Control Theory" color="custom.sunlightYellow" />
            <Typography variant="body1" align="center" sx={{ mt: 2 }}>
              Adaptive watering schedules
            </Typography>
          </motion.div>
        </Grid>
        <Grid item xs={12} md={4}>
          <motion.div whileHover={{ scale: 1.05 }}>
            <PlaceholderImage text="Mobile App" color="custom.soilBrown" />
            <Typography variant="body1" align="center" sx={{ mt: 2 }}>
              Detailed health reporting
            </Typography>
          </motion.div>
        </Grid>
      </Grid>
    ),
  },
  {
    title: 'Product Video',
    content: (
      <Box sx={{ 
        height: '100vh',  // Increased from 85vh to 90vh
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
    title: 'Live Demo',
    content: (
      <Box>
        <Typography variant="h3" gutterBottom>
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
              <Typography variant="body1" paragraph>
                Soil Moisture: 65%
              </Typography>
              <Typography variant="body1" paragraph>
                Temperature: 22°C
              </Typography>
              <Typography variant="body1" paragraph>
                Light: 850 lux
              </Typography>
            </motion.div>
          </Grid>
        </Grid>
      </Box>
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