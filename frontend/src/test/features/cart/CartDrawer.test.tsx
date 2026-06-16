import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi, beforeEach, describe, it, expect } from 'vitest'
import { CartDrawer } from '../../../features/cart/CartDrawer'
import { CartProvider, useCart } from '../../../features/cart/CartContext'
import * as cartApi from '../../../features/cart/cartApi'
import client from '../../../shared/api/client'
import type { CartView } from '../../../features/cart/types'

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
    <CartProvider>
      <TestTrigger cartData={cartData} />
      <CartDrawer />
    </CartProvider>
  )

beforeEach(() => {
  vi.clearAllMocks()
  vi.mocked(client.get).mockResolvedValue({ data: { success: true, data: { restaurant: { restaurantName: 'Test Rest' } } } })
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

    expect(await screen.findByText('2x Burger')).toBeInTheDocument()
    expect(screen.getByText('₹300.00')).toBeInTheDocument()
    expect(screen.getByText('Proceed to Checkout')).toBeInTheDocument()
  })

  it('optimistically removes an item and reconciles', async () => {
    const user = userEvent.setup()
    vi.mocked(cartApi.removeCartItem).mockResolvedValue(mockCartEmpty)

    renderWithProvider(mockCartFilled)
    await user.click(screen.getByText('Open'))
    await user.click(screen.getByText('Sync'))

    const removeBtn = await screen.findByLabelText('Remove item')
    await user.click(removeBtn)

    expect(screen.queryByText('2x Burger')).not.toBeInTheDocument()
    expect(cartApi.removeCartItem).toHaveBeenCalledWith('ci-1')
  })

  it('rolls back state on failure', async () => {
    const user = userEvent.setup()
    vi.mocked(cartApi.removeCartItem).mockRejectedValue(new Error('Network error'))

    renderWithProvider(mockCartFilled)
    await user.click(screen.getByText('Open'))
    await user.click(screen.getByText('Sync'))

    const removeBtn = await screen.findByLabelText('Remove item')
    await user.click(removeBtn)

    expect(screen.queryByText('2x Burger')).not.toBeInTheDocument()
    expect(await screen.findByText('2x Burger')).toBeInTheDocument()
    expect(await screen.findByText('Failed to remove item.')).toBeInTheDocument()
  })
})
