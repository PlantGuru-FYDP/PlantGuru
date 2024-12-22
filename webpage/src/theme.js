import { createTheme } from '@mui/material/styles';

// Colors from Color.kt
const colors = {
  green80: '#4CAF50',    // Primary green
  sage80: '#8BC34A',     // Light sage green
  earth80: '#795548',    // Earthy brown
  moss80: '#33691E',     // Dark moss green
  leafGreen: '#81C784',
  soilBrown: '#3E2723',
  sunlightYellow: '#FDD835',
  waterBlue: '#039BE5',
};

export const theme = createTheme({
  palette: {
    primary: {
      main: colors.sage80,      // Light sage for AppBar
      dark: colors.green80,     // Primary green for accents
    },
    secondary: {
      main: colors.leafGreen,
      dark: colors.moss80,
    },
    tertiary: {
      main: colors.earth80,
    },
    background: {
      default: '#FFFDF7',
      paper: '#F5F3E8',
    },
    text: {
      primary: '#1A1C18',
      secondary: '#1A1C18',
    },
    custom: colors,  // Make all colors available
  },
  typography: {
    fontFamily: "'Raleway', sans-serif",
    h1: {
      fontFamily: "'Righteous', cursive",
    },
    h2: {
      fontFamily: "'Righteous', cursive",
    },
    h3: {
      fontFamily: "'Righteous', cursive",
    },
    h4: {
      fontFamily: "'Righteous', cursive",
    },
    h5: {
      fontFamily: "'Righteous', cursive",
    },
    h6: {
      fontFamily: "'Righteous', cursive",
    },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          backgroundColor: '#FFFDF7',
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: '#FFFFFF',
        },
      },
    },
  },
}); 