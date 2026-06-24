import { useState, useEffect, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box,
  Container,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  Alert,
  CircularProgress,
  Stack,
  Divider,
} from '@mui/material'
import { getProfile, updateProfile, updateAddress } from './api/profileApi'
import { useAuth } from '../../shared/hooks/useAuth'
import { Toast } from '../../shared/ui/Toast'
import { extractErrorMessage } from '../../shared/api/types'
import type { UserProfile, UserAddress } from './types'

export const ProfilePage = () => {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [loading, setLoading] = useState(true)
  const [profile, setProfile] = useState<UserProfile | null>(null)
  
  // Section A: Personal Details State
  const [fullName, setFullName] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [isSavingDetails, setIsSavingDetails] = useState(false)
  const [detailsErrors, setDetailsErrors] = useState<{ fullName?: string; phoneNumber?: string }>({})

  // Section B: Address State
  const [street, setStreet] = useState('')
  const [city, setCity] = useState('')
  const [state, setState] = useState('')
  const [zipcode, setZipcode] = useState('')
  const [country, setCountry] = useState('India')
  const [isSavingAddress, setIsSavingAddress] = useState(false)
  const [addressErrors, setAddressErrors] = useState<{ street?: string; city?: string; state?: string; zipcode?: string; country?: string }>({})

  // Banners & Toast Feedback
  const [toastConfig, setToastConfig] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  })
  const [apiError, setApiError] = useState<string | null>(null)

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        setLoading(true)
        setApiError(null)
        const data = await getProfile()
        setProfile(data)
        setFullName(data.name || '')
        setPhoneNumber(data.phoneNumber || '')
        if (data.address) {
          setStreet(data.address.street || '')
          setCity(data.address.city || '')
          setState(data.address.state || '')
          setZipcode(data.address.zipcode || '')
          setCountry(data.address.country || 'India')
        }
      } catch (err) {
        setApiError(extractErrorMessage(err))
      } finally {
        setLoading(false)
      }
    }
    fetchProfile()
  }, [])

  const handleCloseToast = () => {
    setToastConfig((prev) => ({ ...prev, open: false }))
  }

  const handleSaveDetails = async (e: FormEvent) => {
    e.preventDefault()
    if (isSavingDetails) return

    setDetailsErrors({})
    setApiError(null)

    // Client validation
    const errors: { fullName?: string; phoneNumber?: string } = {}
    if (!fullName || !fullName.trim()) {
      errors.fullName = 'Full Name is required'
    } else if (fullName.length > 100) {
      errors.fullName = 'Name must be 100 characters or less'
    }

    if (phoneNumber && phoneNumber.trim()) {
      const cleanPhone = phoneNumber.trim()
      const phoneRegex = /^[\d\s\-\+]{8,15}$/
      if (!phoneRegex.test(cleanPhone)) {
        errors.phoneNumber = 'Phone number must be between 8 and 15 characters and contain only digits, spaces, dashes, or pluses'
      }
    }

    if (Object.keys(errors).length > 0) {
      setDetailsErrors(errors)
      return
    }

    try {
      setIsSavingDetails(true)
      const updated = await updateProfile({
        fullName: fullName.trim(),
        phoneNumber: phoneNumber ? phoneNumber.trim() : undefined,
      })
      setProfile(updated)
      setFullName(updated.name)
      setPhoneNumber(updated.phoneNumber || '')
      setToastConfig({
        open: true,
        message: 'Profile updated successfully!',
        severity: 'success',
      })
    } catch (err) {
      setApiError(extractErrorMessage(err))
    } finally {
      setIsSavingDetails(false)
    }
  }

  const handleSaveAddress = async (e: FormEvent) => {
    e.preventDefault()
    if (isSavingAddress) return

    setAddressErrors({})
    setApiError(null)

    // Client validation
    const errors: { street?: string; city?: string; state?: string; zipcode?: string; country?: string } = {}
    if (!street || !street.trim()) errors.street = 'Street is required'
    if (!city || !city.trim()) errors.city = 'City is required'
    if (!state || !state.trim()) errors.state = 'State is required'
    if (!zipcode || !zipcode.trim()) errors.zipcode = 'Zip Code is required'
    if (!country || !country.trim()) errors.country = 'Country is required'

    if (Object.keys(errors).length > 0) {
      setAddressErrors(errors)
      return
    }

    const payload: UserAddress = {
      street: street.trim(),
      city: city.trim(),
      state: state.trim(),
      zipcode: zipcode.trim(),
      country: country.trim(),
    }

    try {
      setIsSavingAddress(true)
      const updated = await updateAddress(payload)
      setProfile(updated)
      if (updated.address) {
        setStreet(updated.address.street)
        setCity(updated.address.city)
        setState(updated.address.state)
        setZipcode(updated.address.zipcode)
        setCountry(updated.address.country)
      }
      setToastConfig({
        open: true,
        message: 'Address updated successfully!',
        severity: 'success',
      })
    } catch (err) {
      setApiError(extractErrorMessage(err))
    } finally {
      setIsSavingAddress(false)
    }
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' }}>
        <CircularProgress sx={{ color: '#FF5A5F' }} />
      </Box>
    )
  }

  return (
    <Container maxWidth="md" sx={{ py: 6 }}>
      <Box sx={{ maxWidth: 800, mx: 'auto' }}>
        {apiError && (
          <Alert severity="error" sx={{ mb: 3, borderRadius: '12px' }}>
            {apiError}
          </Alert>
        )}

        {profile && !profile.address && (
          <Alert severity="warning" sx={{ mb: 3, borderRadius: '12px' }}>
            Please add a delivery address below before placing an order.
          </Alert>
        )}

        <Card component="section" sx={{ borderRadius: '16px', boxShadow: '0 4px 20px rgba(0,0,0,0.05)', overflow: 'hidden' }}>
          <Box sx={{ p: 4, bgcolor: '#F9FAFB', borderBottom: '1px solid #E5E7EB' }}>
            <Typography variant="h4" sx={{ fontWeight: 800, color: '#1F2937', mb: 0.5 }}>
              My Profile
            </Typography>
            <Typography variant="body2" sx={{ color: '#6B7280', fontSize: '1.05rem' }}>
              {profile?.email}
            </Typography>
          </Box>

          <CardContent sx={{ p: 4 }}>
            {/* Section A: Personal Details */}
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 3, color: '#1F2937' }}>
              Personal Information
            </Typography>
            <form onSubmit={handleSaveDetails} noValidate>
              <Stack spacing={3} sx={{ mb: 4 }}>
                <TextField
                  label="Full Name"
                  id="fullName"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  error={!!detailsErrors.fullName}
                  helperText={detailsErrors.fullName}
                  disabled={isSavingDetails}
                  fullWidth
                  required
                />
                <TextField
                  label="Phone Number"
                  id="phoneNumber"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  error={!!detailsErrors.phoneNumber}
                  helperText={detailsErrors.phoneNumber}
                  disabled={isSavingDetails}
                  fullWidth
                  placeholder="e.g. +91 98765 43210"
                />
                <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                  <Button
                    type="submit"
                    variant="contained"
                    disabled={isSavingDetails}
                    sx={{
                      bgcolor: '#FF5A5F',
                      '&:hover': { bgcolor: '#E04F54' },
                      textTransform: 'none',
                      fontWeight: 600,
                      px: 4,
                      py: 1.2,
                      borderRadius: '8px',
                    }}
                  >
                    {isSavingDetails ? <CircularProgress size={24} sx={{ color: '#fff' }} /> : 'Save Details'}
                  </Button>
                </Box>
              </Stack>
            </form>

            <Divider sx={{ my: 4 }} />

            {/* Section B: Delivery Address */}
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 3, color: '#1F2937' }}>
              Delivery Address
            </Typography>
            <form onSubmit={handleSaveAddress} noValidate>
              <Stack spacing={3}>
                <TextField
                  label="Street Address"
                  id="street"
                  value={street}
                  onChange={(e) => setStreet(e.target.value)}
                  error={!!addressErrors.street}
                  helperText={addressErrors.street}
                  disabled={isSavingAddress}
                  fullWidth
                  required
                />
                
                <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 3 }}>
                  <TextField
                    label="City"
                    id="city"
                    value={city}
                    onChange={(e) => setCity(e.target.value)}
                    error={!!addressErrors.city}
                    helperText={addressErrors.city}
                    disabled={isSavingAddress}
                    fullWidth
                    required
                  />
                  <TextField
                    label="State"
                    id="state"
                    value={state}
                    onChange={(e) => setState(e.target.value)}
                    error={!!addressErrors.state}
                    helperText={addressErrors.state}
                    disabled={isSavingAddress}
                    fullWidth
                    required
                  />
                </Box>

                <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 3 }}>
                  <TextField
                    label="Zip Code"
                    id="zipcode"
                    value={zipcode}
                    onChange={(e) => setZipcode(e.target.value)}
                    error={!!addressErrors.zipcode}
                    helperText={addressErrors.zipcode}
                    disabled={isSavingAddress}
                    fullWidth
                    required
                  />
                  <TextField
                    label="Country"
                    id="country"
                    value={country}
                    onChange={(e) => setCountry(e.target.value)}
                    error={!!addressErrors.country}
                    helperText={addressErrors.country}
                    disabled={isSavingAddress}
                    fullWidth
                    required
                  />
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
                  <Button
                    type="submit"
                    variant="contained"
                    disabled={isSavingAddress}
                    sx={{
                      bgcolor: '#FF5A5F',
                      '&:hover': { bgcolor: '#E04F54' },
                      textTransform: 'none',
                      fontWeight: 600,
                      px: 4,
                      py: 1.2,
                      borderRadius: '8px',
                    }}
                  >
                    {isSavingAddress ? <CircularProgress size={24} sx={{ color: '#fff' }} /> : 'Save Address'}
                  </Button>
                </Box>
              </Stack>
            </form>

            <Divider sx={{ my: 4 }} />

            {/* Section C: Account Management */}
            <Box sx={{ display: 'flex', justifyContent: 'center' }}>
              <Button
                variant="outlined"
                onClick={handleLogout}
                aria-label="Logout account"
                sx={{
                  color: '#DC2626',
                  borderColor: '#DC2626',
                  '&:hover': { bgcolor: '#FEF2F2', borderColor: '#DC2626' },
                  textTransform: 'none',
                  fontWeight: 600,
                  px: 4,
                  py: 1.2,
                  borderRadius: '8px',
                  width: { xs: '100%', sm: 'auto' },
                }}
              >
                Logout
              </Button>
            </Box>
          </CardContent>
        </Card>
      </Box>

      <Toast
        open={toastConfig.open}
        message={toastConfig.message}
        severity={toastConfig.severity}
        onClose={handleCloseToast}
      />
    </Container>
  )
}
