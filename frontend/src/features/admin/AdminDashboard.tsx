import { useState } from 'react'
import { Box, AppBar, Toolbar, Typography, Button, Container, Tabs, Tab } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../shared/hooks/useAuth'

import { RestaurantProfileForm } from './RestaurantProfileForm'
import { AdminOrderDashboard } from './AdminOrderDashboard'

export function AdminDashboard() {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const [currentTab, setCurrentTab] = useState(0)

  const handleLogout = () => {
    logout()
    navigate('/admin/login')
  }

  return (
    <Box sx={{ 
      minHeight: '100vh', 
      background: 'linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%)',
      position: 'relative',
      zIndex: 0,
      overflowX: 'hidden' // Prevents decorative blobs from causing horizontal scrollbars
    }}>
      {/* Decorative background blobs */}
      <Box sx={{
        position: 'absolute', top: '-10%', left: '-10%', width: '500px', height: '500px',
        background: 'radial-gradient(circle, rgba(211,74,36,0.15) 0%, rgba(253,251,251,0) 70%)',
        zIndex: -1, filter: 'blur(40px)', borderRadius: '50%'
      }} />
      <Box sx={{
        position: 'absolute', bottom: '-10%', right: '-5%', width: '600px', height: '600px',
        background: 'radial-gradient(circle, rgba(250,183,16,0.1) 0%, rgba(253,251,251,0) 70%)',
        zIndex: -1, filter: 'blur(60px)', borderRadius: '50%'
      }} />

      <AppBar position="sticky" elevation={0} sx={{ 
        background: 'rgba(26, 26, 26, 0.8)', 
        backdropFilter: 'blur(12px)',
        borderBottom: '1px solid rgba(255,255,255,0.1)',
        px: { xs: 2, sm: 4 } // Fix unaligned UI
      }}>
        <Toolbar sx={{ maxWidth: '1200px', width: '100%', mx: 'auto' }}>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 800, letterSpacing: '-0.5px' }}>
            Burito<span style={{ color: '#D34A24' }}>Admin</span>
          </Typography>
          <Button color="inherit" onClick={handleLogout} sx={{ textTransform: 'none', fontWeight: 600, borderRadius: '8px' }}>
            Logout
          </Button>
        </Toolbar>
      </AppBar>

      <Container maxWidth="lg" sx={{ py: { xs: 3, md: 4 } }}>
        <Box sx={{ mb: 3, borderBottom: 1, borderColor: 'divider' }}>
          <Tabs 
            value={currentTab} 
            onChange={(_, newValue) => setCurrentTab(newValue)}
            sx={{
              '& .MuiTab-root': {
                fontWeight: 700,
                textTransform: 'none',
                fontSize: '16px',
                color: '#6B7280'
              },
              '& .Mui-selected': {
                color: '#D34A24 !important'
              },
              '& .MuiTabs-indicator': {
                backgroundColor: '#D34A24'
              }
            }}
          >
            <Tab label="Orders" />
            <Tab label="Profile & Menu" />
          </Tabs>
        </Box>

        <Box sx={{ 
          backgroundColor: 'rgba(255, 255, 255, 0.9)', 
          backdropFilter: 'blur(20px)',
          borderRadius: '24px', 
          boxShadow: '0 8px 32px rgba(0,0,0,0.06)',
          border: '1px solid rgba(255,255,255,0.8)',
          overflow: 'hidden'
        }}>
          {currentTab === 0 && <AdminOrderDashboard />}
          {currentTab === 1 && <RestaurantProfileForm />}
        </Box>
      </Container>
    </Box>
  )
}
