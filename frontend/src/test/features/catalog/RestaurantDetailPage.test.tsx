import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { vi, beforeEach } from 'vitest'
import { RestaurantDetailPage } from '../../../features/catalog/RestaurantDetailPage'
import * as catalogApi from '../../../features/catalog/catalogApi'
import type { MenuItem, Restaurant } from '../../../features/catalog/types'

vi.mock('../../../features/catalog/catalogApi')

const mockRestaurant: Restaurant = {
  restaurantId: 'abc-123',
  restaurantName: 'Spice Garden',
  description: null,
  cuisineType: 'INDIAN',
  rating: 4.5,
  estDeliveryMinutes: 30,
  open: true,
  createdAt: '2024-01-01',
  address: null,
}

const mockMenuItem: MenuItem = {
  menuItemId: 'item-1',
  name: 'Butter Chicken',
  description: 'Rich tomato-cream sauce',
  price: 349,
  category: 'MAINS',
  available: true,
}

const renderPage = (id = 'abc-123') =>
  render(
    <MemoryRouter initialEntries={[`/restaurants/${id}`]}>
      <Routes>
        <Route path="/restaurants/:id" element={<RestaurantDetailPage />} />
        <Route path="/restaurants" element={<div>Restaurants list</div>} />
      </Routes>
    </MemoryRouter>,
  )

beforeEach(() => {
  vi.clearAllMocks()
})

describe('RestaurantDetailPage', () => {
  it('renders restaurant info and grouped menu items', async () => {
    vi.mocked(catalogApi.fetchRestaurantWithMenu).mockResolvedValue({
      restaurant: mockRestaurant,
      menuItems: [mockMenuItem],
    })
    renderPage()
    expect(await screen.findByText('Spice Garden')).toBeInTheDocument()
    expect(screen.getByText('INDIAN')).toBeInTheDocument()
    expect(screen.getByText('★ 4.5')).toBeInTheDocument()
    expect(screen.getByText('Butter Chicken')).toBeInTheDocument()
    expect(screen.getByText('Mains')).toBeInTheDocument()
  })

  it('shows Unavailable chip for unavailable items', async () => {
    vi.mocked(catalogApi.fetchRestaurantWithMenu).mockResolvedValue({
      restaurant: mockRestaurant,
      menuItems: [{ ...mockMenuItem, available: false }],
    })
    renderPage()
    expect(await screen.findByText('Unavailable')).toBeInTheDocument()
  })

  it('shows no menu message when menu is empty', async () => {
    vi.mocked(catalogApi.fetchRestaurantWithMenu).mockResolvedValue({
      restaurant: mockRestaurant,
      menuItems: [],
    })
    renderPage()
    expect(await screen.findByText('No menu available yet')).toBeInTheDocument()
  })

  it('shows restaurant not found for 404 errors', async () => {
    vi.mocked(catalogApi.fetchRestaurantWithMenu).mockRejectedValue({ response: { status: 404 } })
    renderPage()
    expect(await screen.findByText('Restaurant not found')).toBeInTheDocument()
    expect(screen.getByText('Back to restaurants')).toBeInTheDocument()
  })

  it('shows generic error message for non-404 errors', async () => {
    vi.mocked(catalogApi.fetchRestaurantWithMenu).mockRejectedValue(new Error('Network error'))
    renderPage()
    expect(
      await screen.findByText('Something went wrong. Please try again.'),
    ).toBeInTheDocument()
    expect(screen.getByText('Back to restaurants')).toBeInTheDocument()
  })
})
