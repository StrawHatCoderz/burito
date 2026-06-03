import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { vi, beforeEach } from 'vitest'
import { RestaurantsPage } from '../../../features/catalog/RestaurantsPage'
import * as catalogApi from '../../../features/catalog/catalogApi'
import type { Restaurant } from '../../../features/catalog/types'

vi.mock('../../../features/catalog/catalogApi')

const mockRestaurant: Restaurant = {
  restaurantId: 'abc-123',
  restaurantName: 'Spice Garden',
  description: 'Authentic Indian cuisine',
  cuisineType: 'INDIAN',
  rating: 4.5,
  estDeliveryMinutes: 30,
  open: true,
  createdAt: '2024-01-01',
  address: null,
}

const renderPage = () =>
  render(
    <MemoryRouter>
      <RestaurantsPage />
    </MemoryRouter>,
  )

beforeEach(() => {
  vi.clearAllMocks()
})

describe('RestaurantsPage', () => {
  it('renders restaurant cards with correct data', async () => {
    vi.mocked(catalogApi.fetchRestaurants).mockResolvedValue([mockRestaurant])
    renderPage()
    expect(await screen.findByText('Spice Garden')).toBeInTheDocument()
    expect(screen.getByText('INDIAN')).toBeInTheDocument()
    expect(screen.getByText('★ 4.5')).toBeInTheDocument()
    expect(screen.getByText('30 min')).toBeInTheDocument()
    expect(screen.getByText('Open')).toBeInTheDocument()
  })

  it('shows closed chip for closed restaurants', async () => {
    vi.mocked(catalogApi.fetchRestaurants).mockResolvedValue([{ ...mockRestaurant, open: false }])
    renderPage()
    expect(await screen.findByText('Closed')).toBeInTheDocument()
  })

  it('shows empty state when list is empty', async () => {
    vi.mocked(catalogApi.fetchRestaurants).mockResolvedValue([])
    renderPage()
    expect(await screen.findByText('No restaurants available right now')).toBeInTheDocument()
  })

  it('shows error message when fetch fails', async () => {
    vi.mocked(catalogApi.fetchRestaurants).mockRejectedValue(new Error('Network error'))
    renderPage()
    expect(
      await screen.findByText('Failed to load restaurants. Please try again.'),
    ).toBeInTheDocument()
  })
})
