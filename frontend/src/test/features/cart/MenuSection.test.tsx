import { render, screen, act } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi, beforeEach, describe, it, expect } from 'vitest'
import { MenuSection } from '../../../features/catalog/MenuSection'
import { CartProvider, useCart } from '../../../features/cart/CartContext'
import * as cartApi from '../../../features/cart/cartApi'
import client from '../../../shared/api/client'
import type { MenuItem } from '../../../features/catalog/types'

vi.mock('../../../features/cart/cartApi')
vi.mock('../../../shared/api/client')

const mockMenuItem: MenuItem = {
  menuItemId: 'item-1',
  name: 'Burger',
  description: 'Tasty',
  price: 150,
  category: 'MAINS',
  available: true,
  imageUrl: null,
}

const mockUnavailableItem: MenuItem = {
  ...mockMenuItem,
  menuItemId: 'item-2',
  name: 'Fries',
  available: false,
}

// A simple test component to read the cart state from context
const CartStateViewer = () => {
  const { cart } = useCart()
  return (
    <div data-testid="cart-state">
      Items: {cart.cartItemCount}, Total: {cart.total}
    </div>
  )
}

const renderWithProvider = (items: MenuItem[]) =>
  render(
    <CartProvider>
      <CartStateViewer />
      <MenuSection category="MAINS" items={items} restaurantOpen={true} />
    </CartProvider>
  )

beforeEach(() => {
  vi.clearAllMocks()
  vi.mocked(client.get).mockResolvedValue({ data: { success: true, data: null } })
})

describe('MenuSection Add to Cart', () => {
  it('shows + Add button for available items, but not for unavailable items', () => {
    renderWithProvider([mockMenuItem, mockUnavailableItem])
    
    // Burger is available
    expect(screen.getByText('Burger')).toBeInTheDocument()
    const addButtons = screen.getAllByText('+ Add')
    expect(addButtons).toHaveLength(1)
    
    // Fries is unavailable
    expect(screen.getByText('Fries')).toBeInTheDocument()
    expect(screen.getByText('Unavailable')).toBeInTheDocument()
  })

  it('optimistically updates the cart and reconciles with backend response on success', async () => {
    const user = userEvent.setup()
    vi.mocked(cartApi.addToCart).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve({
        cartId: 'cart-1',
        restaurantId: 'rest-1',
        items: [{ cartItemId: 'ci-1', menuItemId: 'item-1', name: 'Burger', quantity: 1, unitPrice: 150, subtotal: 150 }],
        total: 150
      }), 10))
    )

    renderWithProvider([mockMenuItem])

    expect(screen.getByTestId('cart-state')).toHaveTextContent('Items: 0, Total: 0')

    const addButton = screen.getByText('+ Add')
    await user.click(addButton)

    // Optimistic state updates immediately
    expect(screen.getByTestId('cart-state')).toHaveTextContent('Items: 1, Total: 150')
    
    // Stepper shows after API resolves (verify by presence of Decrease quantity button)
    expect(await screen.findByRole('button', { name: 'Decrease quantity' })).toBeInTheDocument()
    
    // State is synchronized
    expect(screen.getByTestId('cart-state')).toHaveTextContent('Items: 1, Total: 150')
  })

  it('rolls back optimistic state and shows a toast on failure', async () => {
    const user = userEvent.setup()
    let rejectPromise: (err: any) => void = () => {}
    const promise = new Promise((_, reject) => {
      rejectPromise = reject
    })
    vi.mocked(cartApi.addToCart).mockReturnValue(promise)

    renderWithProvider([mockMenuItem])

    expect(screen.getByTestId('cart-state')).toHaveTextContent('Items: 0, Total: 0')

    const addButton = screen.getByText('+ Add')
    await user.click(addButton)

    // Optimistic update happens
    expect(screen.getByTestId('cart-state')).toHaveTextContent('Items: 1, Total: 150')

    await act(async () => {
      rejectPromise(new Error('Network failure'))
    })

    // Error toast appears
    expect(await screen.findByText('Failed to add item. Please try again.')).toBeInTheDocument()

    // State rolls back
    expect(screen.getByTestId('cart-state')).toHaveTextContent('Items: 0, Total: 0')
  })
})
