import React, { useState, useEffect } from 'react'
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
  Switch,
  FormControlLabel,
  Snackbar,
  Divider,
  SvgIcon
} from '@mui/material'
import type { SvgIconProps } from '@mui/material'
import { getAdminRestaurant, updateAdminRestaurant } from './adminApi'
import type { UpdateRestaurantPayload } from './adminApi'
import { useAuth } from '../../shared/hooks/useAuth'
import { MenuManager } from './MenuManager'

const EditIcon = (props: SvgIconProps) => (
  <SvgIcon {...props}>
    <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" />
  </SvgIcon>
);

const CloseIcon = (props: SvgIconProps) => (
  <SvgIcon {...props}>
    <path d="M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
  </SvgIcon>
);

const StorefrontIcon = (props: SvgIconProps) => (
  <SvgIcon {...props}>
    <path d="M21 7.01V6c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v1.01c-.17.05-.33.14-.46.26l-1.5 1.5c-.32.32-.42.77-.28 1.2l.62 1.87c.18.53.68.89 1.24.89h1v5c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2v-5h1c.56 0 1.06-.36 1.24-.89l.62-1.87c.14-.43.04-.88-.28-1.2l-1.5-1.5c-.13-.12-.29-.21-.46-.26zM18 19H6v-5h12v5zm-2.06-7H8.06l-.42-1.28L8.71 9.66c.21-.21.49-.33.78-.33h5.01c.29 0 .58.11.78.33l1.08 1.06-.42 1.28z" />
  </SvgIcon>
);

export function RestaurantProfileForm() {
  const { restaurantId } = useAuth()
  
  const [formData, setFormData] = useState({
    restaurantName: '',
    cuisineType: '',
    estDeliveryMinutes: '',
    imageUrl: '',
    open: false,
  })
  
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [statusUpdating, setStatusUpdating] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const [isEditMode, setIsEditMode] = useState(false)

  useEffect(() => {
    if (!restaurantId) return

    const loadProfile = async () => {
      try {
        setLoading(true)
        const profile = await getAdminRestaurant(restaurantId)
        setFormData({
          restaurantName: profile.restaurantName || '',
          cuisineType: profile.cuisineType || '',
          estDeliveryMinutes: profile.estDeliveryMinutes ? String(profile.estDeliveryMinutes) : '',
          imageUrl: profile.imageUrl || '',
          open: Boolean(profile.open),
        })
      } catch (err: any) {
        setError('Failed to load profile. ' + (err.response?.data?.message || err.message))
      } finally {
        setLoading(false)
      }
    }
    loadProfile()
  }, [restaurantId])

  const handleChange = (e: any) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
  }

  const handleToggleStatus = async (event: React.ChangeEvent<HTMLInputElement>) => {
    if (!restaurantId) return
    const newStatus = event.target.checked
    setStatusUpdating(true)
    setError(null)
    
    try {
      const payload: UpdateRestaurantPayload = {
        restaurantName: formData.restaurantName,
        cuisineType: formData.cuisineType,
        estDeliveryMinutes: parseInt(formData.estDeliveryMinutes, 10) || 0,
        imageUrl: formData.imageUrl || null,
        open: newStatus
      }
      await updateAdminRestaurant(restaurantId, payload)
      setFormData(prev => ({ ...prev, open: newStatus }))
      setSuccess(true)
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to update status')
    } finally {
      setStatusUpdating(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!restaurantId) return
    setError(null)
    setSaving(true)

    try {
      const payload: UpdateRestaurantPayload = {
        restaurantName: formData.restaurantName,
        cuisineType: formData.cuisineType,
        estDeliveryMinutes: parseInt(formData.estDeliveryMinutes, 10),
        imageUrl: formData.imageUrl || null,
        open: formData.open
      }
      await updateAdminRestaurant(restaurantId, payload)
      setSuccess(true)
      setIsEditMode(false)
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to update profile')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '300px' }}>
        <CircularProgress sx={{ color: '#D34A24' }} />
      </Box>
    )
  }

  return (
    <Box>
      {/* Banner Area */}
      <Box sx={{ 
        height: '100px', 
        width: '100%', 
        backgroundColor: '#f0f0f0',
        backgroundImage: formData.imageUrl ? `url(${formData.imageUrl})` : 'none',
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        position: 'relative'
      }}>
        {!formData.imageUrl && (
          <Box sx={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center', opacity: 0.5 }}>
            <StorefrontIcon sx={{ fontSize: 40 }} />
          </Box>
        )}
        <Box sx={{ 
          position: 'absolute', 
          bottom: 0, 
          left: 0, 
          right: 0, 
          height: '100%',
          background: 'linear-gradient(to top, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0) 100%)',
          display: 'flex',
          alignItems: 'flex-end',
          p: 2
        }}>
          <Typography variant="h5" sx={{ color: 'white', fontWeight: 800 }}>
            {formData.restaurantName || 'Your Restaurant'}
          </Typography>
        </Box>
        
        {/* Edit Mode Toggle Header */}
        <Box sx={{ position: 'absolute', top: 12, right: 16 }}>
          <Button 
            variant="contained" 
            color={isEditMode ? "inherit" : "primary"}
            startIcon={isEditMode ? <CloseIcon /> : <EditIcon />}
            onClick={() => setIsEditMode(!isEditMode)}
            size="small"
            sx={{ 
              borderRadius: '20px', 
              fontWeight: 700, 
              textTransform: 'none',
              boxShadow: '0 4px 12px rgba(0,0,0,0.2)'
            }}
          >
            {isEditMode ? 'Cancel Edit' : 'Edit Profile'}
          </Button>
        </Box>
      </Box>

      {error && <Alert severity="error" sx={{ m: 3 }}>{error}</Alert>}

      <Box sx={{ p: { xs: 3, md: 4 } }}>
        {/* Status indicator row (Now actionable directly from the dashboard) */}
        <Box sx={{ 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'space-between', 
          mb: 4, 
          p: 2, 
          backgroundColor: formData.open ? 'rgba(46, 125, 50, 0.05)' : 'rgba(0, 0, 0, 0.02)',
          borderRadius: { xs: '12px', md: '8px' },
          border: '1px solid',
          borderColor: formData.open ? 'rgba(46, 125, 50, 0.2)' : 'rgba(0, 0, 0, 0.08)'
        }}>
          <Stack direction="row" spacing={2} alignItems="center">
            <Typography variant="h6" fontWeight={700}>Restaurant Status</Typography>
            {statusUpdating ? (
              <CircularProgress size={24} />
            ) : (
              <FormControlLabel
                control={
                  <Switch
                    checked={formData.open}
                    onChange={handleToggleStatus}
                    color="success"
                  />
                }
                label={
                  <Typography fontWeight={700} color={formData.open ? "success.main" : "text.secondary"}>
                    {formData.open ? 'Accepting Orders' : 'Closed'}
                  </Typography>
                }
                sx={{ m: 0 }}
              />
            )}
          </Stack>
          {!isEditMode && (
            <Stack direction="row" spacing={1} alignItems="center">
               <Typography variant="body2" color="text.secondary" fontWeight={600}>
                 {formData.cuisineType} • {formData.estDeliveryMinutes} mins delivery
               </Typography>
            </Stack>
          )}
        </Box>

        {isEditMode && <Divider sx={{ mb: 4 }} />}

        {isEditMode ? (
          <Box component="form" onSubmit={handleSubmit}>
            <Stack spacing={4}>
              <TextField
                required
                fullWidth
                label="Restaurant Name"
                name="restaurantName"
                value={formData.restaurantName}
                onChange={handleChange}
                InputProps={{ sx: { borderRadius: '8px' } }}
              />

              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={3}>
                <FormControl fullWidth required>
                  <InputLabel>Cuisine Type</InputLabel>
                  <Select
                    name="cuisineType"
                    value={formData.cuisineType}
                    label="Cuisine Type"
                    onChange={handleChange}
                    sx={{ borderRadius: '8px' }}
                  >
                    <MenuItem value="ITALIAN">Italian</MenuItem>
                    <MenuItem value="MEXICAN">Mexican</MenuItem>
                    <MenuItem value="ASIAN">Asian</MenuItem>
                    <MenuItem value="AMERICAN">American</MenuItem>
                    <MenuItem value="INDIAN">Indian</MenuItem>
                    <MenuItem value="OTHER">Other</MenuItem>
                  </Select>
                </FormControl>

                <TextField
                  required
                  fullWidth
                  label="Est. Delivery (Mins)"
                  name="estDeliveryMinutes"
                  type="number"
                  inputProps={{ min: 1 }}
                  value={formData.estDeliveryMinutes}
                  onChange={handleChange}
                  InputProps={{ sx: { borderRadius: '8px' } }}
                />
              </Stack>

              <TextField
                fullWidth
                label="Header Image URL"
                name="imageUrl"
                placeholder="https://example.com/image.jpg"
                value={formData.imageUrl}
                onChange={handleChange}
                helperText="Provide a direct link to an image. This will be used as your restaurant's cover photo."
                InputProps={{ sx: { borderRadius: '8px' } }}
              />

              <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, pt: 2 }}>
                <Button 
                  variant="outlined" 
                  onClick={() => setIsEditMode(false)}
                  sx={{ borderRadius: '8px', fontWeight: 600, px: 3 }}
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  disabled={saving}
                  sx={{
                    borderRadius: '8px',
                    fontWeight: 700,
                    px: 4,
                    boxShadow: '0 4px 14px rgba(211, 74, 36, 0.4)'
                  }}
                >
                  {saving ? <CircularProgress size={24} color="inherit" /> : 'Save Changes'}
                </Button>
              </Box>
            </Stack>
          </Box>
        ) : null}
      </Box>

      <Box sx={{ px: { xs: 3, md: 4 }, pb: { xs: 3, md: 4 } }}>
        <MenuManager restaurantId={restaurantId} />
      </Box>

      <Snackbar
        open={success}
        autoHideDuration={4000}
        onClose={() => setSuccess(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={() => setSuccess(false)} severity="success" sx={{ width: '100%', borderRadius: { xs: '8px', md: '4px' } }}>
          Profile updated successfully!
        </Alert>
      </Snackbar>
    </Box>
  )
}

