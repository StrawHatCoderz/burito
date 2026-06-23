import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { RestaurantProfileForm } from '../../../features/admin/RestaurantProfileForm'
import { getAdminRestaurant, updateAdminRestaurant, getAdminMenu } from '../../../features/admin/adminApi'
import { useAuth } from '../../../shared/hooks/useAuth'

vi.mock('../../../features/admin/adminApi')
vi.mock('../../../shared/hooks/useAuth', () => ({
  useAuth: vi.fn(),
}))

describe('RestaurantProfileForm', () => {
  const mockRestaurantId = 'resto-123'
  const mockProfile = {
    restaurantId: mockRestaurantId,
    restaurantName: 'Test Resto',
    cuisineType: 'MEXICAN',
    estDeliveryMinutes: 40,
    imageUrl: 'http://test.com/img.jpg',
    open: true,
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useAuth).mockReturnValue({ restaurantId: mockRestaurantId } as any)
  })

  it('loads and displays the profile', async () => {
    vi.mocked(getAdminRestaurant).mockResolvedValue(mockProfile as any)
    vi.mocked(getAdminMenu).mockResolvedValue([])

    render(<RestaurantProfileForm />)

    expect(screen.getByRole('progressbar')).toBeInTheDocument()

    await waitFor(() => {
      expect(screen.getByText('Test Resto')).toBeInTheDocument()
      expect(screen.getByText('Accepting Orders')).toBeInTheDocument()
    })

    fireEvent.click(screen.getByRole('button', { name: /Edit Profile/i }))

    await waitFor(() => {
      expect(screen.getByDisplayValue('Test Resto')).toBeInTheDocument()
      expect(screen.getByDisplayValue('40')).toBeInTheDocument()
      expect(screen.getByDisplayValue('http://test.com/img.jpg')).toBeInTheDocument()
    })
  })

  it('submits updated profile', async () => {
    vi.mocked(getAdminRestaurant).mockResolvedValue(mockProfile as any)
    vi.mocked(getAdminMenu).mockResolvedValue([])
    vi.mocked(updateAdminRestaurant).mockResolvedValue({ ...mockProfile, restaurantName: 'New Name' } as any)

    render(<RestaurantProfileForm />)

    await waitFor(() => {
      expect(screen.getByText('Test Resto')).toBeInTheDocument()
    })

    fireEvent.click(screen.getByRole('button', { name: /Edit Profile/i }))

    await waitFor(() => {
      expect(screen.getByDisplayValue('Test Resto')).toBeInTheDocument()
    })

    fireEvent.change(screen.getByLabelText(/Restaurant Name/i), { target: { value: 'New Name' } })
    fireEvent.click(screen.getByRole('button', { name: /Save Changes/i }))

    await waitFor(() => {
      expect(updateAdminRestaurant).toHaveBeenCalledWith(mockRestaurantId, {
        restaurantName: 'New Name',
        cuisineType: 'MEXICAN',
        estDeliveryMinutes: 40,
        imageUrl: 'http://test.com/img.jpg',
        open: true
      })
      expect(screen.getByText('Profile updated successfully!')).toBeInTheDocument()
    })
  })

  it('displays error message if fetch fails', async () => {
    vi.mocked(getAdminRestaurant).mockRejectedValue(new Error('Network error'))
    vi.mocked(getAdminMenu).mockResolvedValue([])

    render(<RestaurantProfileForm />)

    await waitFor(() => {
      expect(screen.getByText(/Failed to load profile/i)).toBeInTheDocument()
    })
  })
})
