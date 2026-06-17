import { useState } from 'react'
import CircularProgress from '@mui/material/CircularProgress'
import { useCart } from '../cart/CartContext'
import { addToCart, removeCartItem, decrementCartItem } from '../cart/cartApi'
import { QuantityStepper } from '../../shared/ui/QuantityStepper'
import type { MenuItem } from './types'

interface AddToCartButtonProps {
  item: MenuItem
  onError: (message: string) => void
}

const CartInteraction = ({ item, onError }: AddToCartButtonProps) => {
  const { cart, optimisticAdd, optimisticDecrement, optimisticRemove, syncFromBackend, rollback } = useCart()
  const [status, setStatus] = useState<'idle' | 'loading'>('idle')

  const cartItem = cart.items.find(i => i.menuItemId === item.menuItemId)

  const handleAdd = async () => {
    if (status !== 'idle') return
    
    optimisticAdd(item)
    setStatus('loading')

    try {
      const cartView = await addToCart(item.menuItemId, 1)
      syncFromBackend(cartView)
      setStatus('idle')
    } catch (e) {
      rollback()
      setStatus('idle')
      onError('Failed to add item. Please try again.')
    }
  }

  const handleDecrement = async () => {
    if (!cartItem || status !== 'idle') return
    
    setStatus('loading')
    try {
      if (cartItem.quantity > 1) {
        optimisticDecrement(cartItem.cartItemId)
        const cartView = await decrementCartItem(cartItem.cartItemId)
        syncFromBackend(cartView)
      } else {
        optimisticRemove(cartItem.cartItemId)
        const cartView = await removeCartItem(cartItem.cartItemId)
        syncFromBackend(cartView)
      }
      setStatus('idle')
    } catch (e) {
      rollback()
      setStatus('idle')
      onError('Failed to remove item. Please try again.')
    }
  }

  if (cartItem) {
    return (
      <QuantityStepper
        quantity={cartItem.quantity}
        onIncrement={handleAdd}
        onDecrement={handleDecrement}
        isLoading={status === 'loading'}
      />
    )
  }

  return (
    <button
      onClick={handleAdd}
      disabled={status === 'loading'}
      className="bg-accent text-bg-surface px-4 py-2 rounded-md font-body font-semibold text-sm transition-all hover:bg-[#B33F1F] active:scale-95 disabled:opacity-70 disabled:cursor-not-allowed flex items-center justify-center min-w-[80px]"
    >
      {status === 'loading' ? <CircularProgress size={16} color="inherit" /> : '+ Add'}
    </button>
  )
}

interface MenuItemCardProps {
  item: MenuItem
  onError: (message: string) => void
}

export const MenuItemCard = ({ item, onError }: MenuItemCardProps) => {
  return (
    <div
      className={`flex items-start justify-between gap-4 p-4 bg-bg-surface rounded-lg border border-border ${
        !item.available 
          ? 'opacity-60 grayscale-[0.5]' 
          : 'hover:shadow-md hover:border-accent-subtle transition-all duration-200 ease-in-out'
      }`}
    >
      {item.imageUrl && (
        <div className="shrink-0 w-24 h-24 sm:w-32 sm:h-32 rounded-lg overflow-hidden bg-gray-100 border border-border">
          <img 
            src={item.imageUrl} 
            alt={item.name} 
            className="w-full h-full object-cover" 
            loading="lazy" 
          />
        </div>
      )}
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 flex-wrap">
          <h4 className="text-base font-body font-semibold text-text-primary">
            {item.name}
          </h4>
          {!item.available && (
            <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-border text-text-muted">
              Unavailable
            </span>
          )}
        </div>
        {item.description && (
          <p className="mt-1 text-sm text-text-muted font-body leading-relaxed">
            {item.description}
          </p>
        )}
      </div>
      
      <div className="flex flex-col items-end gap-3 shrink-0">
        <span className="text-base font-body font-semibold text-text-primary">
          ₹{item.price.toFixed(2)}
        </span>
        {item.available && <CartInteraction item={item} onError={onError} />}
      </div>
    </div>
  )
}
