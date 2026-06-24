import { useState, useEffect, useCallback } from 'react'
import Drawer from '@mui/material/Drawer'
import Typography from '@mui/material/Typography'
import IconButton from '@mui/material/IconButton'
import Button from '@mui/material/Button'
import Skeleton from '@mui/material/Skeleton'
import { QuantityStepper } from '../../shared/ui/QuantityStepper'
import { useCart } from './CartContext'
import { addToCart, removeCartItem, decrementCartItem, clearCart } from './cartApi'
import { checkoutCart } from '../orders/api/orders.api'
import client from '../../shared/api/client'
import { Toast } from '../../shared/ui/Toast'
import { useAuth } from '../../shared/hooks/useAuth'
import { useRestaurantSocket } from '../../shared/hooks/useRestaurantSocket'
import { useNavigate } from 'react-router-dom'
import { extractErrorMessage } from '../../shared/api/types'

const CartItemRow = ({ item, onError, restaurantOpen }: { item: any; onError: (msg: string) => void; restaurantOpen: boolean; isUnavailable?: boolean }) => {
  const { optimisticAdd, optimisticDecrement, optimisticRemove, syncFromBackend, rollback } = useCart()
  const [status, setStatus] = useState<'idle' | 'loading'>('idle')

  const handleAdd = async () => {
    if (status !== 'idle') return
    optimisticAdd({ menuItemId: item.menuItemId, name: item.name, price: item.unitPrice } as any)
    setStatus('loading')
    try {
      const cartView = await addToCart(item.menuItemId, 1)
      syncFromBackend(cartView)
      setStatus('idle')
    } catch (e) {
      rollback()
      setStatus('idle')
      onError('Failed to add item.')
    }
  }

  const handleDecrement = async () => {
    if (status !== 'idle') return
    setStatus('loading')
    try {
      if (item.quantity > 1) {
        optimisticDecrement(item.cartItemId)
        const cartView = await decrementCartItem(item.cartItemId)
        syncFromBackend(cartView)
      } else {
        optimisticRemove(item.cartItemId)
        const cartView = await removeCartItem(item.cartItemId)
        syncFromBackend(cartView)
      }
      setStatus('idle')
    } catch (e) {
      rollback()
      setStatus('idle')
      onError('Failed to remove item.')
    }
  }

  return (
    <div className="flex flex-col gap-3 p-4 bg-white rounded-xl md:rounded-lg border border-gray-100 shadow-sm transition-all">
      <div className="flex justify-between items-start gap-2">
        <Typography variant="body1" fontWeight={600} className="flex-1 leading-snug">
          {item.name}
        </Typography>
        <Typography variant="body1" fontWeight={700}>
          ₹{item.subtotal.toFixed(2)}
        </Typography>
      </div>
      <div className="flex justify-between items-center">
        <Typography variant="body2" color="textSecondary" fontWeight={500}>
          ₹{item.unitPrice.toFixed(2)} / each
        </Typography>
        <QuantityStepper
          size="small"
          quantity={item.quantity}
          onIncrement={handleAdd}
          onDecrement={handleDecrement}
          isLoading={status === 'loading'}
          incrementDisabled={!restaurantOpen}
        />
      </div>
    </div>
  )
}

export const CartDrawer = () => {
  const { cart, isCartOpen, closeCartDrawer, optimisticClear, syncFromBackend, rollback } = useCart()
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [restaurantName, setRestaurantName] = useState<string | null>(null)
  const [loadingName, setLoadingName] = useState(false)
  const [isRestaurantOpen, setIsRestaurantOpen] = useState(true)
  const [unavailableItemIds, setUnavailableItemIds] = useState<Set<string>>(new Set())
  const [toastConfig, setToastConfig] = useState<{ open: boolean; message: string; severity: 'error' | 'success' }>({ open: false, message: '', severity: 'error' })
  const [isCheckingOut, setIsCheckingOut] = useState(false)

  const closeToast = () => setToastConfig(prev => ({ ...prev, open: false }))
  const showError = (message: string) => setToastConfig({ open: true, message, severity: 'error' })
  const showSuccess = (message: string) => setToastConfig({ open: true, message, severity: 'success' })

  useEffect(() => {
    if (cart.restaurantId && isCartOpen && !restaurantName) {
      setLoadingName(true)
      client.get(`/restaurants/${cart.restaurantId}`).then(res => {
        const data = res.data.data
        if (data && data.restaurant) {
          setRestaurantName(data.restaurant.restaurantName)
          setIsRestaurantOpen(data.restaurant.open)
        }
        setLoadingName(false)
      }).catch(() => setLoadingName(false))
    }
  }, [cart.restaurantId, isCartOpen, restaurantName])

  useEffect(() => {
    if (cart.items.length === 0) {
      setRestaurantName(null)
      setUnavailableItemIds(new Set())
    }
  }, [cart.items.length])

  // Live restaurant availability — flip checkout guard without page reload
  const handleAvailability = useCallback((event: { open: boolean }) => {
    setIsRestaurantOpen(event.open)
  }, [])

  // Live menu changes — flag deleted/unavailable cart items
  const handleMenuEvent = useCallback((event: { type: string; item?: { menuItemId: string; available: boolean }; menuItemId?: string }) => {
    const { type, item, menuItemId } = event
    setUnavailableItemIds((prev) => {
      const next = new Set(prev)
      if (type === 'ITEM_DELETED' && menuItemId) {
        next.add(menuItemId)
      } else if (type === 'ITEM_AVAILABILITY_CHANGED' && item) {
        if (!item.available) {
          next.add(item.menuItemId)
        } else {
          next.delete(item.menuItemId)
        }
      }
      return next
    })
  }, [])

  useRestaurantSocket({
    restaurantId: cart.restaurantId ?? null,
    onAvailability: handleAvailability,
    onMenu: handleMenuEvent,
  })

  const handleClear = async () => {
    optimisticClear()
    try {
      const response = await clearCart()
      syncFromBackend(response)
    } catch (e) {
      rollback()
      showError('Failed to clear cart.')
    }
  }

  const handleCheckout = async () => {
    if (!isAuthenticated) {
      return
    }

    setIsCheckingOut(true)
    try {
      await checkoutCart()
      showSuccess('Order placed successfully!')
      optimisticClear()
      closeCartDrawer()
      navigate('/orders/active')
    } catch (e: any) {
      showError(extractErrorMessage(e))
    } finally {
      setIsCheckingOut(false)
    }
  }

  const isEmpty = cart.items.length === 0

  return (
    <>
      <Drawer
        anchor="right"
        open={isCartOpen}
        onClose={closeCartDrawer}
        PaperProps={{
          sx: { width: { xs: '100vw', sm: 400 }, bgcolor: '#F9FAFB' }
        }}
      >
        <div className="flex items-center justify-between p-4 bg-white border-b border-gray-200 sticky top-0 z-10">
          <Typography variant="h6" fontWeight={700} color="#1F2937">
            Your Cart
          </Typography>
          <IconButton onClick={closeCartDrawer} aria-label="Close cart">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </IconButton>
        </div>

        <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-4">
          {isEmpty ? (
            <div className="flex flex-col items-center justify-center flex-1 h-full text-center mt-20 gap-4">
              <div className="w-24 h-24 rounded-full bg-gray-100 flex items-center justify-center text-gray-400">
                <svg viewBox="0 0 24 24" width="48" height="48" fill="currentColor">
                  <path d="M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zm10 0c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2zm-9.83-3.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49A1.003 1.003 0 0020 4H5.21l-.94-2H1v2h2l3.6 7.59-1.35 2.44C4.52 15.37 5.48 17 7 17h12v-2H7l1.1-2h7.45z"/>
                </svg>
              </div>
              <Typography variant="h6" fontWeight={600} color="textSecondary">
                Your cart is empty
              </Typography>
              <Typography variant="body2" color="textSecondary" className="mb-4">
                Looks like you haven't added anything yet.
              </Typography>
              <Button
                variant="outlined"
                onClick={closeCartDrawer}
                sx={{
                  borderRadius: '9999px',
                  textTransform: 'none',
                  fontWeight: 600,
                  borderColor: '#FF5A5F',
                  color: '#FF5A5F',
                  '&:hover': {
                    borderColor: '#E03C31',
                    bgcolor: 'rgba(255, 90, 95, 0.04)',
                  }
                }}
              >
                Continue Shopping
              </Button>
            </div>
          ) : (
            <>
              {loadingName ? (
                <Skeleton variant="text" width={200} height={32} />
              ) : restaurantName ? (
                <div className="flex items-center justify-between">
                  <Typography variant="subtitle2" color="textSecondary" fontWeight={600} className="uppercase tracking-wider">
                    From {restaurantName}
                  </Typography>
                  <Button size="small" color="error" onClick={handleClear} sx={{ textTransform: 'none', fontWeight: 500 }}>
                    Clear
                  </Button>
                </div>
              ) : null}

              {cart.items.map(item => (
                <CartItemRow
                  key={item.cartItemId}
                  item={item}
                  onError={showError}
                  restaurantOpen={isRestaurantOpen}
                  isUnavailable={unavailableItemIds.has(item.menuItemId)}
                />
              ))}
            </>
          )}
        </div>

        {!isEmpty && (
          <div className="p-4 bg-white border-t border-gray-200 sticky bottom-0 z-10 flex flex-col gap-4 shadow-[0_-4px_6px_-1px_rgba(0,0,0,0.05)]">
            <div className="flex justify-between items-center">
              <Typography variant="h6" fontWeight={700}>
                Total
              </Typography>
              <Typography variant="h6" fontWeight={800} color="#FF5A5F">
                ₹{cart.total.toFixed(2)}
              </Typography>
            </div>

            {unavailableItemIds.size > 0 && (
              <div
                role="status"
                className="flex items-start gap-2 px-3 py-2.5 rounded-xl md:rounded-lg border text-sm font-body"
                style={{ background: '#FFF0F0', borderColor: '#FFCDCD', color: '#991B1B' }}
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" className="shrink-0 mt-0.5" aria-hidden="true">
                  <path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z" />
                </svg>
                <span>
                  Some items in your cart are <strong>no longer available</strong>. Remove them to continue.
                </span>
              </div>
            )}

            <Button
              fullWidth
              variant="contained"
              onClick={handleCheckout}
              disabled={isCheckingOut || !isAuthenticated || !isRestaurantOpen || unavailableItemIds.size > 0}
              sx={{
                borderRadius: '9999px',
                py: 1.5,
                fontWeight: 700,
                textTransform: 'none',
                fontSize: '16px',
                bgcolor: '#FF5A5F',
                '&:hover': { bgcolor: '#E03C31' },
                '&.Mui-disabled': {
                  bgcolor: '#FF9A9E',
                  color: '#fff'
                }
              }}
            >
              {isCheckingOut
                ? 'Processing...'
                : !isRestaurantOpen
                ? 'Restaurant Closed'
                : unavailableItemIds.size > 0
                ? 'Items Unavailable'
                : 'Proceed to Checkout'}
            </Button>
            {!isAuthenticated && (
              <Typography variant="body2" color="textSecondary" align="center" sx={{ mt: 1 }}>
                Please <span onClick={() => { closeCartDrawer(); navigate('/login?redirect=cart'); }} style={{ color: '#FF5A5F', cursor: 'pointer', fontWeight: 600 }}>login</span> to checkout
              </Typography>
            )}
          </div>
        )}
      </Drawer>
      <Toast open={toastConfig.open} message={toastConfig.message} severity={toastConfig.severity} onClose={closeToast} />
    </>
  )
}
