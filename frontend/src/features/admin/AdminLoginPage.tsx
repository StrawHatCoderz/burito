import React, { useState } from 'react'
import { useNavigate, Link as RouterLink } from 'react-router-dom'
import {
  Box,
  Typography,
  TextField,
  Button,
  Alert,
  CircularProgress,
  Stack,
  Link,
  useTheme,
  useMediaQuery,
  InputAdornment,
  IconButton
} from '@mui/material'
import { adminLogin } from '../../shared/api/authApi'
import { useAuth } from '../../shared/hooks/useAuth'
import bgImage from '../../assets/admin_register_bg.webp'

export function AdminLoginPage() {
  const navigate = useNavigate()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))
  const { login } = useAuth()
  
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)

    try {
      const response = await adminLogin({ email, password })
      login(response.accessToken, response.refreshToken, true)
      navigate('/admin/dashboard')
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Login failed')
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
              Welcome back to Burito
            </Typography>
            <Typography variant="h6" sx={{ opacity: 0.9, fontWeight: 400 }}>
              Access your restaurant dashboard, manage orders, and grow your business.
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
          <Box sx={{ mb: 5 }}>
            <Typography component="h1" variant="h4" fontWeight={800} sx={{ mb: 1, color: '#1A1A1A' }}>
              Sign In
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Enter your credentials to access your restaurant dashboard.
            </Typography>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 3, borderRadius: '8px' }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%' }}>
            <Stack spacing={2.5}>
              <TextField
                required
                fullWidth
                label="Email Address"
                name="email"
                type="email"
                autoFocus
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                InputProps={{ sx: { borderRadius: '8px' } }}
              />
              <TextField
                required
                fullWidth
                label="Password"
                name="password"
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
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
            </Stack>

            <Box sx={{ mt: 2, textAlign: 'right' }}>
              <Link component={RouterLink} to="#" sx={{ color: 'text.secondary', fontSize: '0.875rem', textDecoration: 'none', '&:hover': { color: '#D34A24' } }}>
                Forgot Password?
              </Link>
            </Box>

            <Button
              type="submit"
              fullWidth
              variant="contained"
              disabled={loading}
              disableElevation
              sx={{ 
                mt: 3, 
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
              {loading ? <CircularProgress size={24} color="inherit" /> : 'Sign In'}
            </Button>

            <Box sx={{ textAlign: 'center' }}>
              <Typography variant="body2" color="text.secondary">
                Don't have an admin account?{' '}
                <Link component={RouterLink} to="/admin/register" sx={{ color: '#D34A24', fontWeight: 600, textDecoration: 'none', '&:hover': { textDecoration: 'underline' } }}>
                  Register here
                </Link>
              </Typography>
            </Box>
          </Box>
        </Box>
      </Box>
    </Box>
  )
}
