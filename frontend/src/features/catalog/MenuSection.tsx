import { useState } from 'react'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import CircularProgress from '@mui/material/CircularProgress'
import Typography from '@mui/material/Typography'
import { useCart } from '../cart/CartContext'
import { addToCart } from '../cart/cartApi'
import { Toast } from '../../shared/ui/Toast'
import type { MenuItem } from './types'

interface MenuSectionProps {
  category: string
  items: MenuItem[]
}

const formatLabel = (category: string) =>
  category.charAt(0) + category.slice(1).toLowerCase()

interface AddToCartButtonProps {
  item: MenuItem
  onError: (message: string) => void
}

const AddToCartButton = ({ item, onError }: AddToCartButtonProps) => {
  const { optimisticAdd, syncFromBackend, rollback } = useCart()
  const [status, setStatus] = useState<'idle' | 'loading' | 'success'>('idle')

  const handleClick = async () => {
    if (status !== 'idle') return
    
    // 1. Optimistic local update
    optimisticAdd(item)
    setStatus('loading')

    try {
      // 2. API call
      const cartView = await addToCart(item.menuItemId, 1)
      
      // 3. Backend reconciliation
      syncFromBackend(cartView)
      setStatus('success')
      
      // Reset button state after 1.5s
      setTimeout(() => setStatus('idle'), 1500)
    } catch (e) {
      // 4. Rollback on failure
      rollback()
      setStatus('idle')
      onError('Failed to add item. Please try again.')
    }
  }

  if (status === 'success') {
    return (
      <Button
        variant="contained"
        disabled
        sx={{
          borderRadius: '9999px',
          bgcolor: '#10B981 !important', // Emerald Green
          color: '#fff !important',
          fontWeight: 600,
          textTransform: 'none',
          minWidth: '90px'
        }}
      >
        Added ✓
      </Button>
    )
  }

  return (
    <Button
      variant="contained"
      onClick={handleClick}
      disabled={status === 'loading'}
      sx={{
        borderRadius: '9999px',
        bgcolor: '#FF5A5F', // Tomato/Coral
        color: '#fff',
        fontWeight: 600,
        textTransform: 'none',
        minWidth: '90px',
        '&:hover': {
          bgcolor: '#E03C31',
          transform: 'translateY(-1px)',
          boxShadow: '0 4px 12px rgba(255, 90, 95, 0.3)',
        },
        transition: 'all 0.2s ease',
      }}
    >
      {status === 'loading' ? <CircularProgress size={20} color="inherit" /> : '+ Add'}
    </Button>
  )
}

export const MenuSection = ({ category, items }: MenuSectionProps) => {
  const [errorToast, setErrorToast] = useState<{ open: boolean; message: string }>({ open: false, message: '' })

  const handleError = (message: string) => setErrorToast({ open: true, message })
  const closeToast = () => setErrorToast((prev) => ({ ...prev, open: false }))

  return (
    <div className="mb-6">
      <Typography variant="h6" component="h3" fontWeight={600} gutterBottom>
        {formatLabel(category)}
      </Typography>
      <div className="flex flex-col gap-3">
        {items.map((item) => (
          <div
            key={item.menuItemId}
            className={`flex items-start justify-between gap-4 p-4 bg-white rounded-2xl border ${
              !item.available ? 'opacity-60' : 'hover:shadow-md transition-shadow duration-200 ease-in-out'
            }`}
          >
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2 flex-wrap">
                <Typography variant="subtitle1" fontWeight={600}>
                  {item.name}
                </Typography>
                {!item.available && (
                  <Chip label="Unavailable" size="small" variant="outlined" />
                )}
              </div>
              {item.description && (
                <Typography variant="body2" color="text.secondary" className="mt-1">
                  {item.description}
                </Typography>
              )}
            </div>
            
            <div className="flex flex-col items-end gap-2 shrink-0">
              <Typography variant="body1" fontWeight={600}>
                ₹{item.price.toFixed(2)}
              </Typography>
              {item.available && <AddToCartButton item={item} onError={handleError} />}
            </div>
          </div>
        ))}
      </div>
      <Toast open={errorToast.open} message={errorToast.message} onClose={closeToast} />
    </div>
  )
}
