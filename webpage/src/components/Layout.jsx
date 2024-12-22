import { Outlet, Link as RouterLink, useLocation } from 'react-router-dom';
import { AppBar, Toolbar, Typography, Button, Container, Box, IconButton } from '@mui/material';
import { styled } from '@mui/material/styles';
import GitHubIcon from '@mui/icons-material/GitHub';
import Logo from './Logo';

const StyledToolbar = styled(Toolbar)(({ theme }) => ({
  display: 'flex',
  justifyContent: 'space-between',
  padding: theme.spacing(0, 3),
}));

const NavButton = styled(Button)(({ theme }) => ({
  color: theme.palette.primary.dark,
  marginLeft: theme.spacing(2),
  '&:hover': {
    backgroundColor: theme.palette.primary.dark,
    color: theme.palette.common.white,
  },
}));

const LogoContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  textDecoration: 'none',
  color: theme.palette.primary.dark,
}));

const LogoText = styled(Typography)(({ theme }) => ({
  fontFamily: "'Righteous', cursive",
  color: theme.palette.primary.dark,
  marginLeft: theme.spacing(1),
}));

export default function Layout() {
  const location = useLocation();
  const hash = location.hash.slice(1);

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar position="static" elevation={0}>
        <StyledToolbar>
          <LogoContainer component={RouterLink} to="/">
            <Logo sx={{ color: 'primary.dark', width: 32, height: 32 }} />
            <LogoText variant="h5">
              PlantGuru
            </LogoText>
          </LogoContainer>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <NavButton component={RouterLink} to="/display">
              Display
            </NavButton>
            <NavButton component={RouterLink} to={`/docs${hash ? '#' + hash : ''}`}>
              Documentation
            </NavButton>
            <NavButton component={RouterLink} to="/live">
              Live Monitoring
            </NavButton>
            <IconButton
              component="a"
              href="https://github.com/PlantGuru-FYDP/PlantGuru"
              target="_blank"
              rel="noopener noreferrer"
              sx={{ 
                ml: 2,
                color: 'primary.dark',
                '&:hover': {
                  backgroundColor: 'primary.dark',
                  color: 'common.white',
                }
              }}
            >
              <GitHubIcon />
            </IconButton>
          </Box>
        </StyledToolbar>
      </AppBar>
      <Container component="main" sx={{ flexGrow: 1, py: 4 }}>
        <Outlet />
      </Container>
    </Box>
  );
} 