import { HashRouter, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { theme } from './theme';
import Layout from './components/Layout';
import Home from './pages/Home';
import Display from './pages/Display';
import Documentation from './pages/Documentation';
import LiveMonitoring from './pages/LiveMonitoring';
import DisplayPresentation from './pages/DisplayPresentation';

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <HashRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<Home />} />
            <Route path="presentationLaunch" element={<Display />} />
            <Route path="docs" element={<Documentation />} />
            <Route path="live" element={<LiveMonitoring />} />
          </Route>
          <Route path="/presentation" element={<DisplayPresentation />} />
        </Routes>
      </HashRouter>
    </ThemeProvider>
  );
}

export default App;
