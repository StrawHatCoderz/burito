import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import { AdminLoginPage } from './AdminLoginPage'
import { adminLogin } from '../../shared/api/authApi'
import { useAuth } from '../../shared/hooks/useAuth'

vi.mock('../../shared/api/authApi')
vi.mock('../../shared/hooks/useAuth')

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate
  }
})

describe('AdminLoginPage', () => {
  const mockLogin = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useAuth).mockReturnValue({ login: mockLogin } as any)
  })

  it('renders correctly', () => {
    render(
      <MemoryRouter>
        <AdminLoginPage />
      </MemoryRouter>
    )
    expect(screen.getByRole('heading', { name: /Sign In/i })).toBeInTheDocument()
    expect(screen.getByLabelText(/Email Address/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^Password/i)).toBeInTheDocument()
  })

  it('handles successful login', async () => {
    vi.mocked(adminLogin).mockResolvedValue({ data: { accessToken: 'admin-token', refreshToken: 'admin-refresh-token' } } as any)
    
    render(
      <MemoryRouter>
        <AdminLoginPage />
      </MemoryRouter>
    )

    fireEvent.change(screen.getByLabelText(/Email Address/i), { target: { value: 'admin@test.com' } })
    fireEvent.change(screen.getByLabelText(/^Password/i), { target: { value: 'password123' } })
    fireEvent.click(screen.getByRole('button', { name: /Sign In/i }))

    await waitFor(() => {
      expect(adminLogin).toHaveBeenCalledWith({ email: 'admin@test.com', password: 'password123' })
    })
    expect(mockLogin).toHaveBeenCalledWith('admin-token', 'admin-refresh-token', true)
    expect(mockNavigate).toHaveBeenCalledWith('/admin/dashboard')
  })

  it('displays error on failed login', async () => {
    vi.mocked(adminLogin).mockRejectedValue({ response: { data: { message: 'Invalid credentials' } } })
    
    render(
      <MemoryRouter>
        <AdminLoginPage />
      </MemoryRouter>
    )

    fireEvent.change(screen.getByLabelText(/Email Address/i), { target: { value: 'admin@test.com' } })
    fireEvent.change(screen.getByLabelText(/^Password/i), { target: { value: 'wrongpass' } })
    fireEvent.click(screen.getByRole('button', { name: /Sign In/i }))

    expect(await screen.findByText('Invalid credentials')).toBeInTheDocument()
    expect(mockLogin).not.toHaveBeenCalled()
    expect(mockNavigate).not.toHaveBeenCalled()
  })
})
