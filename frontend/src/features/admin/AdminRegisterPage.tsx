import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import {
  Container,
  Box,
  Typography,
  TextField,
  Button,
  Select,
  MenuItem,
  InputLabel,
  FormControl,
  Alert,
  CircularProgress
} from '@mui/material'
import { adminRegister } from '../../shared/api/authApi'

export function AdminRegisterPage() {
  const navigate = useNavigate()
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
        ...formData,
        estimated_delivery_minutes: parseInt(formData.estimated_delivery_minutes, 10)
      }
      await adminRegister(payload)
      navigate('/admin/login')
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Container maxWidth="sm">
      <Box sx={{ mt: 8, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <Typography component="h1" variant="h5" sx={{ mb: 3 }}>
          Restaurant Admin Registration
        </Typography>

        {error && <Alert severity="error" sx={{ width: '100%', mb: 2 }}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1, width: '100%' }}>
          <TextField
            margin="normal"
            required
            fullWidth
            label="Full Name"
            name="full_name"
            autoFocus
            value={formData.full_name}
            onChange={handleChange}
          />
          <TextField
            margin="normal"
            required
            fullWidth
            label="Email Address"
            name="email"
            type="email"
            value={formData.email}
            onChange={handleChange}
          />
          <TextField
            margin="normal"
            required
            fullWidth
            label="Password"
            name="password"
            type="password"
            value={formData.password}
            onChange={handleChange}
          />
          <TextField
            margin="normal"
            required
            fullWidth
            label="Restaurant Name"
            name="restaurant_name"
            value={formData.restaurant_name}
            onChange={handleChange}
          />
          <FormControl fullWidth margin="normal" required>
            <InputLabel>Cuisine Type</InputLabel>
            <Select
              name="cuisine_type"
              value={formData.cuisine_type}
              label="Cuisine Type"
              onChange={handleChange}
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
            margin="normal"
            required
            fullWidth
            label="Estimated Delivery Minutes"
            name="estimated_delivery_minutes"
            type="number"
            inputProps={{ min: 1 }}
            value={formData.estimated_delivery_minutes}
            onChange={handleChange}
          />
          
          <Button
            type="submit"
            fullWidth
            variant="contained"
            disabled={loading}
            sx={{ mt: 3, mb: 2, py: 1.5 }}
          >
            {loading ? <CircularProgress size={24} color="inherit" /> : 'Register Restaurant'}
          </Button>

          <Box sx={{ textAlign: 'center' }}>
            <Link to="/admin/login" style={{ textDecoration: 'none', color: '#D34A24' }}>
              Already have an admin account? Sign In
            </Link>
          </Box>
        </Box>
      </Box>
    </Container>
  )
}
