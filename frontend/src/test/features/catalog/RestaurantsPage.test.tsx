import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { vi, beforeEach } from 'vitest'
import { RestaurantsPage } from '../../../features/catalog/RestaurantsPage'
import * as catalogApi from '../../../features/catalog/catalogApi'
import type { Restaurant } from '../../../features/catalog/types'

vi.mock('../../../features/catalog/catalogApi')

vi.mock('../../../shared/hooks/useDebounce', () => ({
  useDebounce: (value: unknown) => value,
}))

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
  imageUrl: null,
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
    expect(await screen.findByText('No restaurants found')).toBeInTheDocument()
  })

  it('shows error message when fetch fails', async () => {
    vi.mocked(catalogApi.fetchRestaurants).mockRejectedValue(new Error('Network error'))
    renderPage()
    expect(
      await screen.findByText('Failed to load restaurants. Please try again.'),
    ).toBeInTheDocument()
  })

  it('renders search input and cuisine dropdown', async () => {
    vi.mocked(catalogApi.fetchRestaurants).mockResolvedValue([])
    renderPage()
    await screen.findByText('No restaurants found')
    expect(screen.getByRole('textbox')).toBeInTheDocument()
    expect(screen.getByRole('combobox')).toBeInTheDocument()
  })

  it('calls fetchRestaurants with search param when input has value', async () => {
    vi.mocked(catalogApi.fetchRestaurants).mockResolvedValue([])
    renderPage()
    await screen.findByText('No restaurants found')
    vi.mocked(catalogApi.fetchRestaurants).mockClear()

    fireEvent.change(screen.getByRole('textbox'), { target: { value: 'spicy' } })

    await waitFor(() => {
      expect(catalogApi.fetchRestaurants).toHaveBeenCalledWith(
        expect.objectContaining({ search: 'spicy' }),
      )
    })
  })

  it('calls fetchRestaurants with no search param when input is cleared', async () => {
    vi.mocked(catalogApi.fetchRestaurants).mockResolvedValue([])
    renderPage()
    await screen.findByText('No restaurants found')

    fireEvent.change(screen.getByRole('textbox'), { target: { value: 'spicy' } })
    // wait for component to settle before clearing
    await screen.findByText('No restaurants found')
    vi.mocked(catalogApi.fetchRestaurants).mockClear()

    fireEvent.change(screen.getByRole('textbox'), { target: { value: '' } })

    await waitFor(() => {
      expect(catalogApi.fetchRestaurants).toHaveBeenCalledWith(
        expect.objectContaining({ search: undefined }),
      )
    })
  })

  it('calls fetchRestaurants with cuisine param when cuisine is selected', async () => {
    vi.mocked(catalogApi.fetchRestaurants).mockResolvedValue([])
    renderPage()
    await screen.findByText('No restaurants found')
    vi.mocked(catalogApi.fetchRestaurants).mockClear()

    fireEvent.mouseDown(screen.getByRole('combobox'))
    fireEvent.click(await screen.findByRole('option', { name: 'INDIAN' }))

    await waitFor(() => {
      expect(catalogApi.fetchRestaurants).toHaveBeenCalledWith(
        expect.objectContaining({ cuisine: 'INDIAN' }),
      )
    })
  })
})
