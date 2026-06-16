import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { ThemeProvider, createTheme } from '@mui/material/styles'
import CssBaseline from '@mui/material/CssBaseline'
import './index.css'
import App from './App.tsx'
import { CartProvider } from './features/cart/CartContext'

const theme = createTheme({
  palette: {
    primary: {
      main: '#D34A24', // Harissa Red
    },
  },
  typography: {
    fontFamily: '"DM Sans", "Roboto", "Helvetica", "Arial", sans-serif',
  },
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <CartProvider>
        <App />
      </CartProvider>
    </ThemeProvider>
  </StrictMode>,
)
