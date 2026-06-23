import { render, screen, act } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi, beforeEach, describe, it, expect } from 'vitest'
import { CartDrawer } from '../../../features/cart/CartDrawer'
import { CartProvider, useCart } from '../../../features/cart/CartContext'
import * as cartApi from '../../../features/cart/cartApi'
import client from '../../../shared/api/client'
import type { CartView } from '../../../features/cart/types'

import { AuthProvider } from '../../../shared/context/AuthContext'
import { MemoryRouter } from 'react-router-dom'

vi.mock('../../../features/cart/cartApi')
vi.mock('../../../shared/api/client')

const mockCartEmpty: CartView = {
  cartId: 'cart-1',
  restaurantId: null,
  items: [],
  total: 0,
}

const mockCartFilled: CartView = {
  cartId: 'cart-1',
  restaurantId: 'rest-1',
  items: [
    { cartItemId: 'ci-1', menuItemId: 'item-1', name: 'Burger', quantity: 2, unitPrice: 150, subtotal: 300 }
  ],
  total: 300,
}

const mockCartSingleItem: CartView = {
  cartId: 'cart-1',
  restaurantId: 'rest-1',
  items: [
    { cartItemId: 'ci-1', menuItemId: 'item-1', name: 'Burger', quantity: 1, unitPrice: 150, subtotal: 150 }
  ],
  total: 150,
}

const TestTrigger = ({ cartData }: { cartData: CartView }) => {
  const { openCartDrawer, syncFromBackend } = useCart()
  
  return (
    <>
      <button onClick={openCartDrawer}>Open</button>
      <button onClick={() => syncFromBackend(cartData)}>Sync</button>
    </>
  )
}

const renderWithProvider = (cartData: CartView) =>
  render(
    <MemoryRouter>
      <AuthProvider>
        <CartProvider>
          <TestTrigger cartData={cartData} />
          <CartDrawer />
        </CartProvider>
      </AuthProvider>
    </MemoryRouter>
  )

beforeEach(() => {
  vi.clearAllMocks()
  vi.mocked(client.get).mockResolvedValue({ data: { success: true, data: { restaurant: { restaurantName: 'Test Rest', open: true } } } })
})

describe('CartDrawer', () => {
  it('shows empty state when no items are present', async () => {
    const user = userEvent.setup()
    renderWithProvider(mockCartEmpty)
    
    await user.click(screen.getByText('Open'))
    await user.click(screen.getByText('Sync'))

    expect(await screen.findByText('Your cart is empty')).toBeInTheDocument()
    expect(screen.getByText('Continue Shopping')).toBeInTheDocument()
  })

  it('renders line items and total correctly', async () => {
    const user = userEvent.setup()
    renderWithProvider(mockCartFilled)
    
    await user.click(screen.getByText('Open'))
    await user.click(screen.getByText('Sync'))

    expect(await screen.findByText('Burger')).toBeInTheDocument()
    expect(screen.getByText('2')).toBeInTheDocument()
    expect(screen.getAllByText('₹300.00')).toHaveLength(2)
    expect(screen.getByText('Proceed to Checkout')).toBeInTheDocument()
  })

  it('optimistically removes an item and reconciles', async () => {
    const user = userEvent.setup()
    vi.mocked(cartApi.removeCartItem).mockResolvedValue(mockCartEmpty)

    renderWithProvider(mockCartSingleItem)
    await user.click(screen.getByText('Open'))
    await user.click(screen.getByText('Sync'))

    const removeBtn = await screen.findByLabelText('Decrease quantity')
    await user.click(removeBtn)

    expect(screen.queryByText('Burger')).not.toBeInTheDocument()
    expect(cartApi.removeCartItem).toHaveBeenCalledWith('ci-1')
  })

  it('rolls back state on failure', async () => {
    const user = userEvent.setup()
    let rejectPromise: (err: any) => void = () => {}
    const promise = new Promise((_, reject) => {
      rejectPromise = reject
    })
    vi.mocked(cartApi.removeCartItem).mockReturnValue(promise)

    renderWithProvider(mockCartSingleItem)
    await user.click(screen.getByText('Open'))
    await user.click(screen.getByText('Sync'))

    const removeBtn = await screen.findByLabelText('Decrease quantity')
    await user.click(removeBtn)

    expect(screen.queryByText('Burger')).not.toBeInTheDocument()

    await act(async () => {
      rejectPromise(new Error('Network error'))
    })

    expect(await screen.findByText('Burger')).toBeInTheDocument()
    expect(await screen.findByText('Failed to remove item.')).toBeInTheDocument()
  })
})
