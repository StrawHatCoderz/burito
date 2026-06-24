import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import { ProfilePage } from './ProfilePage'
import { getProfile, updateProfile, updateAddress } from './api/profileApi'
import { useAuth } from '../../shared/hooks/useAuth'

vi.mock('./api/profileApi')
vi.mock('../../shared/hooks/useAuth')

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate
  }
})

describe('ProfilePage', () => {
  const mockLogout = vi.fn()

  const mockProfile = {
    id: '123',
    email: 'deadpool@test.com',
    name: 'Wade Wilson',
    phoneNumber: '+1234567890',
    address: {
      street: '123 Main St',
      city: 'Bengaluru',
      state: 'Karnataka',
      zipcode: '560001',
      country: 'India',
    },
    createdAt: '2023-01-01',
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useAuth).mockReturnValue({ logout: mockLogout, isAuthenticated: true } as any)
  })

  it('renders loading state initially', async () => {
    vi.mocked(getProfile).mockReturnValue(new Promise(() => {})) // never resolves
    render(
      <MemoryRouter>
        <ProfilePage />
      </MemoryRouter>
    )
    expect(screen.getByRole('progressbar')).toBeInTheDocument()
  })

  it('renders profile details correctly after fetching', async () => {
    vi.mocked(getProfile).mockResolvedValue(mockProfile)

    render(
      <MemoryRouter>
        <ProfilePage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.getByText('deadpool@test.com')).toBeInTheDocument()
    })

    expect(screen.getByLabelText(/Full Name/i)).toHaveValue('Wade Wilson')
    expect(screen.getByLabelText(/Phone Number/i)).toHaveValue('+1234567890')
    expect(screen.getByLabelText(/Street Address/i)).toHaveValue('123 Main St')
    expect(screen.getByLabelText(/City/i)).toHaveValue('Bengaluru')
    expect(screen.getByLabelText(/State/i)).toHaveValue('Karnataka')
    expect(screen.getByLabelText(/Zip Code/i)).toHaveValue('560001')
    expect(screen.getByLabelText(/Country/i)).toHaveValue('India')
    expect(screen.queryByText(/Please add a delivery address below before placing an order./i)).not.toBeInTheDocument()
  })

  it('shows warning alert when address is missing', async () => {
    const profileNoAddress = { ...mockProfile, address: null }
    vi.mocked(getProfile).mockResolvedValue(profileNoAddress)

    render(
      <MemoryRouter>
        <ProfilePage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.getByText(/Please add a delivery address below before placing an order./i)).toBeInTheDocument()
    })
  })

  it('validates personal details fields correctly', async () => {
    vi.mocked(getProfile).mockResolvedValue(mockProfile)

    render(
      <MemoryRouter>
        <ProfilePage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.getByLabelText(/Full Name/i)).toHaveValue('Wade Wilson')
    })

    // Try submitting with empty name
    fireEvent.change(screen.getByLabelText(/Full Name/i), { target: { value: '' } })
    fireEvent.click(screen.getByRole('button', { name: /Save Details/i }))

    await waitFor(() => {
      expect(screen.getByText(/Full Name is required/i)).toBeInTheDocument()
    })

    // Try submitting with invalid phone format
    fireEvent.change(screen.getByLabelText(/Full Name/i), { target: { value: 'Wade' } })
    fireEvent.change(screen.getByLabelText(/Phone Number/i), { target: { value: 'invalid-phone' } })
    fireEvent.click(screen.getByRole('button', { name: /Save Details/i }))

    await waitFor(() => {
      expect(screen.getByText(/Phone number must be/i)).toBeInTheDocument()
    })
  })

  it('validates address fields correctly on save address submit', async () => {
    vi.mocked(getProfile).mockResolvedValue(mockProfile)

    render(
      <MemoryRouter>
        <ProfilePage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.getByLabelText(/Street Address/i)).toHaveValue('123 Main St')
    })

    fireEvent.change(screen.getByLabelText(/Street Address/i), { target: { value: '' } })
    fireEvent.change(screen.getByLabelText(/City/i), { target: { value: '' } })
    fireEvent.change(screen.getByLabelText(/State/i), { target: { value: '' } })
    fireEvent.change(screen.getByLabelText(/Zip Code/i), { target: { value: '' } })
    fireEvent.change(screen.getByLabelText(/Country/i), { target: { value: '' } })

    fireEvent.click(screen.getByRole('button', { name: /Save Address/i }))

    await waitFor(() => {
      expect(screen.getByText(/Street is required/i)).toBeInTheDocument()
      expect(screen.getByText(/City is required/i)).toBeInTheDocument()
      expect(screen.getByText(/State is required/i)).toBeInTheDocument()
      expect(screen.getByText(/Zip Code is required/i)).toBeInTheDocument()
      expect(screen.getByText(/Country is required/i)).toBeInTheDocument()
    })
  })

  it('calls updateProfile and updateAddress APIs on valid submissions', async () => {
    vi.mocked(getProfile).mockResolvedValue(mockProfile)
    vi.mocked(updateProfile).mockResolvedValue(mockProfile)
    vi.mocked(updateAddress).mockResolvedValue(mockProfile)

    render(
      <MemoryRouter>
        <ProfilePage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.getByLabelText(/Full Name/i)).toHaveValue('Wade Wilson')
    })

    // Save details
    fireEvent.change(screen.getByLabelText(/Full Name/i), { target: { value: 'Deadpool' } })
    fireEvent.click(screen.getByRole('button', { name: /Save Details/i }))

    await waitFor(() => {
      expect(updateProfile).toHaveBeenCalledWith({ fullName: 'Deadpool', phoneNumber: '+1234567890' })
    })

    // Save address
    fireEvent.change(screen.getByLabelText(/Street Address/i), { target: { value: '456 Main St' } })
    fireEvent.click(screen.getByRole('button', { name: /Save Address/i }))

    await waitFor(() => {
      expect(updateAddress).toHaveBeenCalledWith({
        street: '456 Main St',
        city: 'Bengaluru',
        state: 'Karnataka',
        zipcode: '560001',
        country: 'India',
      })
    })
  })

  it('triggers logout action correctly on logout click', async () => {
    vi.mocked(getProfile).mockResolvedValue(mockProfile)

    render(
      <MemoryRouter>
        <ProfilePage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Logout/i })).toBeInTheDocument()
    })

    fireEvent.click(screen.getByRole('button', { name: /Logout/i }))

    expect(mockLogout).toHaveBeenCalled()
    expect(mockNavigate).toHaveBeenCalledWith('/login')
  })
})
