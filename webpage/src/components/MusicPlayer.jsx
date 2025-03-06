import { Box, IconButton, Paper, Typography } from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import PauseIcon from '@mui/icons-material/Pause';
import MusicNoteIcon from '@mui/icons-material/MusicNote';
import { useState, useRef } from 'react';

export default function MusicPlayer() {
  const [isPlaying, setIsPlaying] = useState(false);
  const audioRef = useRef(new Audio('/PlantGuru/music/PlantGuru.mp3'));

  const togglePlay = () => {
    if (isPlaying) {
      audioRef.current.pause();
    } else {
      audioRef.current.play();
    }
    setIsPlaying(!isPlaying);
  };

  return (
    <Box sx={{ width: '100%', maxWidth: '600px' }}>
      <Typography 
        variant="subtitle1" 
        align="center" 
        gutterBottom 
        sx={{ 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center',
          gap: 1,
          color: '#1b5e20'
        }}
      >
        <MusicNoteIcon /> Listen to the PlantGuru Song Now
      </Typography>
      <Paper 
        elevation={3}
        sx={{ 
          bgcolor: '#e8f5e9',
          p: 2,
          border: 2,
          borderColor: '#1b5e20',
          borderRadius: 2
        }}
      >
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 3,
            p: 2,
            bgcolor: 'white',
            borderRadius: 1
          }}
        >
          <IconButton 
            onClick={togglePlay}
            sx={{
              color: '#1b5e20',
              '&:hover': {
                bgcolor: '#e8f5e9'
              }
            }}
            size="large"
          >
            {isPlaying ? <PauseIcon /> : <PlayArrowIcon />}
          </IconButton>
          <Typography 
            variant="body1"
            sx={{ 
              color: '#1b5e20',
              fontWeight: 500
            }}
          >
            PlantGuru Theme Song
          </Typography>
        </Box>
      </Paper>
    </Box>
  );
} 