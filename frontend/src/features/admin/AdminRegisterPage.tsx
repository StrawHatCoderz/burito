import React, { useState } from 'react'
import { useNavigate, Link as RouterLink } from 'react-router-dom'
import {
  Box,
  Typography,
  TextField,
  Button,
  Select,
  MenuItem,
  InputLabel,
  FormControl,
  Alert,
  CircularProgress,
  Stack,
  Link,
  useTheme,
  useMediaQuery,
  InputAdornment,
  IconButton
} from '@mui/material'
import { adminRegister, adminLogin } from '../../shared/api/authApi'
import { useAuth } from '../../shared/hooks/useAuth'
import bgImage from '../../assets/admin_register_bg.webp'

export function AdminRegisterPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))
  const [formData, setFormData] = useState({
    full_name: '',
    email: '',
    password: '',
    restaurant_name: '',
    cuisine_type: '',
    estimated_delivery_minutes: ''
  })
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  const handleChange = (e: any) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)

    try {
      const payload = {
        fullName: formData.full_name,
        email: formData.email,
        password: formData.password,
        restaurantName: formData.restaurant_name,
        cuisineType: formData.cuisine_type.toUpperCase(),
        estDeliveryMinutes: parseInt(formData.estimated_delivery_minutes, 10)
      }
      await adminRegister(payload)
      const loginResponse = await adminLogin({ email: formData.email, password: formData.password })
      login(loginResponse.accessToken, true)
      navigate('/admin/dashboard')
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', backgroundColor: '#FAFAFA' }}>
      {/* Left side: Beautiful image */}
      {!isMobile && (
        <Box
          sx={{
            flex: 1,
            position: 'relative',
            backgroundImage: `url(${bgImage})`,
            backgroundSize: 'cover',
            backgroundPosition: 'center',
            '&::before': {
              content: '""',
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              background: 'linear-gradient(to bottom, rgba(0,0,0,0.2) 0%, rgba(0,0,0,0.8) 100%)',
            }
          }}
        >
          <Box
            sx={{
              position: 'absolute',
              bottom: 0,
              left: 0,
              p: 6,
              color: 'white',
              maxWidth: '600px'
            }}
          >
            <Typography variant="h3" fontWeight={700} gutterBottom sx={{ fontFamily: 'DM Sans, sans-serif' }}>
              Partner with Burito
            </Typography>
            <Typography variant="h6" sx={{ opacity: 0.9, fontWeight: 400 }}>
              Join thousands of restaurants growing their business and reaching new customers every day.
            </Typography>
          </Box>
        </Box>
      )}

      {/* Right side: Form */}
      <Box
        sx={{
          flex: { xs: 1, md: '0 0 550px' },
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
          p: { xs: 4, sm: 6, md: 8 },
          backgroundColor: '#FFFFFF',
          boxShadow: isMobile ? 'none' : '-10px 0px 30px rgba(0,0,0,0.05)',
          zIndex: 1
        }}
      >
        <Box sx={{ width: '100%', maxWidth: '420px' }}>
          <Box sx={{ mb: 4 }}>
            <Typography component="h1" variant="h4" fontWeight={800} sx={{ mb: 1, color: '#1A1A1A' }}>
              Create an account
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Let's get your restaurant set up and ready to serve.
            </Typography>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 3, borderRadius: '8px' }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%' }}>
            <Stack spacing={2.5}>
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2.5}>
                <TextField
                  required
                  fullWidth
                  label="Full Name"
                  name="full_name"
                  value={formData.full_name}
                  onChange={handleChange}
                  InputProps={{ sx: { borderRadius: '8px' } }}
                />
                <TextField
                  required
                  fullWidth
                  label="Email Address"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleChange}
                  InputProps={{ sx: { borderRadius: '8px' } }}
                />
              </Stack>

              <TextField
                required
                fullWidth
                label="Password"
                name="password"
                type={showPassword ? 'text' : 'password'}
                value={formData.password}
                onChange={handleChange}
                InputProps={{ 
                  sx: { borderRadius: '8px' },
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        aria-label="toggle password visibility"
                        onClick={() => setShowPassword(!showPassword)}
                        edge="end"
                      >
                        <Typography variant="body2" sx={{ color: '#D34A24', fontWeight: 600, mr: 1, cursor: 'pointer' }}>
                          {showPassword ? 'HIDE' : 'SHOW'}
                        </Typography>
                      </IconButton>
                    </InputAdornment>
                  )
                }}
              />

              <TextField
                required
                fullWidth
                label="Restaurant Name"
                name="restaurant_name"
                value={formData.restaurant_name}
                onChange={handleChange}
                InputProps={{ sx: { borderRadius: '8px' } }}
              />

              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2.5}>
                <FormControl fullWidth required>
                  <InputLabel>Cuisine Type</InputLabel>
                  <Select
                    name="cuisine_type"
                    value={formData.cuisine_type}
                    label="Cuisine Type"
                    onChange={handleChange}
                    sx={{ borderRadius: '8px' }}
                  >
                    <MenuItem value="Italian">Italian</MenuItem>
                    <MenuItem value="Mexican">Mexican</MenuItem>
                    <MenuItem value="Asian">Asian</MenuItem>
                    <MenuItem value="American">American</MenuItem>
                    <MenuItem value="Indian">Indian</MenuItem>
                    <MenuItem value="Other">Other</MenuItem>
                  </Select>
                </FormControl>

                <TextField
                  required
                  fullWidth
                  label="Est. Delivery (Mins)"
                  name="estimated_delivery_minutes"
                  type="number"
                  inputProps={{ min: 1 }}
                  value={formData.estimated_delivery_minutes}
                  onChange={handleChange}
                  InputProps={{ sx: { borderRadius: '8px' } }}
                />
              </Stack>
            </Stack>

            <Button
              type="submit"
              fullWidth
              variant="contained"
              disabled={loading}
              disableElevation
              sx={{ 
                mt: 4, 
                mb: 3, 
                py: 1.6, 
                borderRadius: '8px',
                fontWeight: 700,
                fontSize: '1rem',
                textTransform: 'none',
                transition: 'all 0.2s ease-in-out',
                '&:hover': {
                  transform: 'translateY(-2px)',
                  boxShadow: '0 8px 20px -6px rgba(211, 74, 36, 0.4)'
                }
              }}
            >
              {loading ? <CircularProgress size={24} color="inherit" /> : 'Register Restaurant'}
            </Button>

            <Box sx={{ textAlign: 'center' }}>
              <Typography variant="body2" color="text.secondary">
                Already have an admin account?{' '}
                <Link component={RouterLink} to="/admin/login" sx={{ color: '#D34A24', fontWeight: 600, textDecoration: 'none', '&:hover': { textDecoration: 'underline' } }}>
                  Sign In
                </Link>
              </Typography>
            </Box>
          </Box>
        </Box>
      </Box>
    </Box>
  )
}
