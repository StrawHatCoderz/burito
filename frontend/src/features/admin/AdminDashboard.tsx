import React from 'react'
import { Box, AppBar, Toolbar, Typography, Button, Container, useTheme } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../shared/hooks/useAuth'

export function AdminDashboard() {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const theme = useTheme()

  const handleLogout = () => {
    logout()
    navigate('/admin/login')
  }

  return (
    <Box sx={{ minHeight: '100vh', backgroundColor: '#FAFAFA' }}>
      <AppBar position="static" elevation={0} sx={{ backgroundColor: '#1A1A1A', borderBottom: '1px solid #333' }}>
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 700, fontFamily: 'DM Sans, sans-serif' }}>
            Burito Admin
          </Typography>
          <Button color="inherit" onClick={handleLogout} sx={{ textTransform: 'none', fontWeight: 600 }}>
            Logout
          </Button>
        </Toolbar>
      </AppBar>
      <Container maxWidth="md" sx={{ py: 6 }}>
        <Typography variant="h4" fontWeight={800} sx={{ mb: 4, color: '#1A1A1A' }}>
          Restaurant Profile
        </Typography>
        <Box sx={{ p: 4, backgroundColor: '#FFFFFF', borderRadius: '12px', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
          <Typography variant="body1" color="text.secondary">
            The profile form will be implemented here.
          </Typography>
        </Box>
      </Container>
    </Box>
  )
}
