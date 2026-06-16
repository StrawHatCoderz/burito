import { createContext, useContext, useState, useCallback, ReactNode, useEffect } from 'react'
import type { CartState, CartView } from './types'
import type { MenuItem } from '../catalog/types'
import client from '../../shared/api/client'
import type { ApiResponse } from '../../shared/api/types'

const initialState: CartState = {
  cartId: null,
  restaurantId: null,
  items: [],
  total: 0,
  cartItemCount: 0,
}

interface CartContextValue {
  cart: CartState
  optimisticAdd: (menuItem: MenuItem) => void
  syncFromBackend: (cartView: CartView) => void
  rollback: () => void
  refreshCart: () => Promise<void>
}

const CartContext = createContext<CartContextValue | undefined>(undefined)

export const useCart = () => {
  const context = useContext(CartContext)
  if (!context) throw new Error('useCart must be used within CartProvider')
  return context
}

export const CartProvider = ({ children }: { children: ReactNode }) => {
  const [cart, setCart] = useState<CartState>(initialState)
  const [snapshot, setSnapshot] = useState<CartState | null>(null)

  const syncFromBackend = useCallback((cartView: CartView) => {
    setCart({
      ...cartView,
      cartItemCount: cartView.items.reduce((sum, item) => sum + item.quantity, 0),
    })
    setSnapshot(null)
  }, [])

  const refreshCart = useCallback(async () => {
    try {
      const { data } = await client.get<ApiResponse<CartView>>('/cart')
      if (data.data) {
        syncFromBackend(data.data)
      }
    } catch (e) {
      // Ignore initial load failures (e.g. 404 when cart is empty)
    }
  }, [syncFromBackend])

  // Initial load
  useEffect(() => {
    refreshCart()
  }, [refreshCart])

  const optimisticAdd = useCallback((menuItem: MenuItem) => {
    setCart((prev) => {
      setSnapshot(prev) // Save snapshot exactly as it was before this update

      const existingItemIndex = prev.items.findIndex(
        (i) => i.menuItemId === menuItem.menuItemId
      )
      const newItems = [...prev.items]

      if (existingItemIndex >= 0) {
        const item = newItems[existingItemIndex]
        newItems[existingItemIndex] = {
          ...item,
          quantity: item.quantity + 1,
          subtotal: item.subtotal + menuItem.price,
        }
      } else {
        newItems.push({
          cartItemId: 'temp-' + Date.now(),
          menuItemId: menuItem.menuItemId,
          name: menuItem.name,
          quantity: 1,
          unitPrice: menuItem.price,
          subtotal: menuItem.price,
        })
      }

      return {
        ...prev,
        items: newItems,
        total: prev.total + menuItem.price,
        cartItemCount: prev.cartItemCount + 1,
      }
    })
  }, [])

  const rollback = useCallback(() => {
    setCart((prev) => {
      if (snapshot) {
        const reverted = snapshot
        setSnapshot(null)
        return reverted
      }
      return prev
    })
  }, [snapshot])

  return (
    <CartContext.Provider
      value={{
        cart,
        optimisticAdd,
        syncFromBackend,
        rollback,
        refreshCart,
      }}
    >
      {children}
    </CartContext.Provider>
  )
}
