import React, { useState, useEffect } from 'react'
import {
  Drawer,
  Box,
  Typography,
  TextField,
  Button,
  Select,
  MenuItem as SelectItem,
  InputLabel,
  FormControl,
  Stack,
  Switch,
  FormControlLabel,
  IconButton,
  Divider,
  SvgIcon,
} from '@mui/material'
import type { SvgIconProps } from '@mui/material'
import type { MenuItemPayload } from './adminApi'
import type { MenuItem } from '../catalog/types'
import { extractErrorMessage } from '../../shared/api/types'

const CloseIcon = (props: SvgIconProps) => (
  <SvgIcon {...props}>
    <path d="M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
  </SvgIcon>
)

interface MenuItemFormProps {
  open: boolean
  onClose: () => void
  initialData?: MenuItem | null
  onSubmit: (data: MenuItemPayload) => Promise<void>
}

const CATEGORIES = ['STARTERS', 'MAINS', 'SIDES', 'DESSERTS', 'BEVERAGES', 'OTHER']

export function MenuItemForm({ open, onClose, initialData, onSubmit }: MenuItemFormProps) {
  const [formData, setFormData] = useState<MenuItemPayload>({
    name: '',
    description: '',
    price: 0,
    category: 'MAINS',
    isAvailable: true,
    imageUrl: '',
  })
  
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (open) {
      if (initialData) {
        setFormData({
          name: initialData.name,
          description: initialData.description || '',
          price: initialData.price,
          category: initialData.category,
          isAvailable: initialData.available,
          imageUrl: initialData.imageUrl || '',
        })
      } else {
        setFormData({
          name: '',
          description: '',
          price: 0,
          category: 'MAINS',
          isAvailable: true,
          imageUrl: '',
        })
      }
      setError(null)
    }
  }, [open, initialData])

  const handleChange = (e: any) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  const handleToggleAvailable = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData((prev) => ({
      ...prev,
      isAvailable: e.target.checked,
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    // Basic validation
    if (!formData.name.trim() || formData.price < 0) {
      setError('Please provide a valid name and positive price.')
      return
    }

    try {
      setError(null)
      setIsSubmitting(true)
      
      const payload: MenuItemPayload = {
        ...formData,
        description: formData.description?.trim() || null,
        imageUrl: formData.imageUrl?.trim() || null,
        price: Number(formData.price),
      }
      
      await onSubmit(payload)
      onClose()
    } catch (err: any) {
      setError(extractErrorMessage(err))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Drawer
      anchor="right"
      open={open}
      onClose={onClose}
      PaperProps={{
        sx: { 
          width: { xs: '100%', sm: 400 },
          p: 3,
          borderTopLeftRadius: '24px',
          borderBottomLeftRadius: '24px',
          backgroundColor: '#fafafa'
        }
      }}
    >
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h5" fontWeight={800} color="text.primary">
          {initialData ? 'Edit Item' : 'Add New Item'}
        </Typography>
        <IconButton onClick={onClose} edge="end" sx={{ backgroundColor: 'rgba(0,0,0,0.04)' }}>
          <CloseIcon />
        </IconButton>
      </Box>
      <Divider sx={{ mb: 3 }} />

      <Box component="form" onSubmit={handleSubmit}>
        <Stack spacing={3}>
          {error && <Typography color="error" variant="body2">{error}</Typography>}

          <TextField
            required
            fullWidth
            label="Item Name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            InputProps={{ sx: { borderRadius: '12px', backgroundColor: 'white' } }}
          />

          <TextField
            fullWidth
            label="Description"
            name="description"
            multiline
            rows={3}
            value={formData.description}
            onChange={handleChange}
            InputProps={{ sx: { borderRadius: '12px', backgroundColor: 'white' } }}
          />

          <Stack direction="row" spacing={2}>
            <TextField
              required
              fullWidth
              label="Price (₹)"
              name="price"
              type="number"
              inputProps={{ min: 0, step: 0.01 }}
              value={formData.price}
              onChange={handleChange}
              InputProps={{ sx: { borderRadius: '12px', backgroundColor: 'white' } }}
            />

            <FormControl fullWidth required>
              <InputLabel>Category</InputLabel>
              <Select
                name="category"
                value={formData.category}
                label="Category"
                onChange={handleChange}
                sx={{ borderRadius: '12px', backgroundColor: 'white' }}
              >
                {CATEGORIES.map((cat) => (
                  <SelectItem key={cat} value={cat}>
                    {cat.charAt(0) + cat.slice(1).toLowerCase()}
                  </SelectItem>
                ))}
              </Select>
            </FormControl>
          </Stack>

          <TextField
            fullWidth
            label="Image URL"
            name="imageUrl"
            placeholder="https://example.com/dish.jpg"
            value={formData.imageUrl}
            onChange={handleChange}
            InputProps={{ sx: { borderRadius: '12px', backgroundColor: 'white' } }}
            helperText="A direct link to a delicious photo of the dish."
          />

          <Box sx={{ 
            p: 2, 
            backgroundColor: formData.isAvailable ? 'rgba(46, 125, 50, 0.05)' : 'rgba(0,0,0,0.04)',
            borderRadius: '12px',
            border: '1px solid',
            borderColor: formData.isAvailable ? 'rgba(46, 125, 50, 0.2)' : 'rgba(0,0,0,0.1)'
          }}>
            <FormControlLabel
              control={
                <Switch
                  checked={formData.isAvailable}
                  onChange={handleToggleAvailable}
                  color="success"
                />
              }
              label={
                <Typography fontWeight={600} color={formData.isAvailable ? 'success.main' : 'text.secondary'}>
                  {formData.isAvailable ? 'Available on Menu' : 'Hidden / Sold Out'}
                </Typography>
              }
              sx={{ m: 0 }}
            />
          </Box>

          <Box sx={{ display: 'flex', gap: 2, pt: 2, pb: 4 }}>
            <Button
              variant="outlined"
              fullWidth
              onClick={onClose}
              sx={{ borderRadius: '12px', fontWeight: 700, py: 1.5 }}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="contained"
              fullWidth
              disabled={isSubmitting}
              sx={{
                borderRadius: '12px',
                fontWeight: 700,
                py: 1.5,
                boxShadow: '0 4px 14px rgba(211, 74, 36, 0.4)',
                backgroundColor: '#D34A24',
                '&:hover': {
                  backgroundColor: '#B33A1A'
                }
              }}
            >
              {isSubmitting ? 'Saving...' : (initialData ? 'Save Changes' : 'Add Item')}
            </Button>
          </Box>
        </Stack>
      </Box>
    </Drawer>
  )
}
