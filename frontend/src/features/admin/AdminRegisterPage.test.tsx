import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import { AdminRegisterPage } from './AdminRegisterPage'
import { adminRegister } from '../../shared/api/authApi'

vi.mock('../../shared/api/authApi')

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate
  }
})

describe('AdminRegisterPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders correctly', () => {
    render(
      <MemoryRouter>
        <AdminRegisterPage />
      </MemoryRouter>
    )
    expect(screen.getByRole('heading', { name: /Create an account/i })).toBeInTheDocument()
    expect(screen.getByLabelText(/Full Name/i)).toBeInTheDocument()
  })

  it('handles successful registration', async () => {
    vi.mocked(adminRegister).mockResolvedValue({ success: true })
    
    render(
      <MemoryRouter>
        <AdminRegisterPage />
      </MemoryRouter>
    )

    fireEvent.change(screen.getByLabelText(/Full Name/i), { target: { value: 'John Doe' } })
    fireEvent.change(screen.getByLabelText(/Email Address/i), { target: { value: 'admin@test.com' } })
    fireEvent.change(screen.getByLabelText(/^Password/i), { target: { value: 'password123' } })
    fireEvent.change(screen.getByLabelText(/Restaurant Name/i), { target: { value: 'My Resto' } })
    
    const select = screen.getByRole('combobox')
    fireEvent.mouseDown(select)
    const option = await screen.findByRole('option', { name: 'Italian' })
    fireEvent.click(option)

    fireEvent.change(screen.getByLabelText(/Est. Delivery \(Mins\)/i), { target: { value: '30' } })
    
    fireEvent.click(screen.getByRole('button', { name: /Register Restaurant/i }))

    await waitFor(() => {
      expect(adminRegister).toHaveBeenCalledWith({
        full_name: 'John Doe',
        email: 'admin@test.com',
        password: 'password123',
        restaurant_name: 'My Resto',
        cuisine_type: 'Italian',
        estimated_delivery_minutes: 30
      })
    })
    expect(mockNavigate).toHaveBeenCalledWith('/admin/login')
  })

  it('displays error on failed registration', async () => {
    vi.mocked(adminRegister).mockRejectedValue({ response: { data: { message: 'Email taken' } } })
    
    render(
      <MemoryRouter>
        <AdminRegisterPage />
      </MemoryRouter>
    )

    fireEvent.change(screen.getByLabelText(/Full Name/i), { target: { value: 'John' } })
    fireEvent.change(screen.getByLabelText(/Email Address/i), { target: { value: 'admin@test.com' } })
    fireEvent.change(screen.getByLabelText(/^Password/i), { target: { value: 'password123' } })
    fireEvent.change(screen.getByLabelText(/Restaurant Name/i), { target: { value: 'My Resto' } })
    fireEvent.change(screen.getByLabelText(/Est. Delivery \(Mins\)/i), { target: { value: '30' } })
    
    // Select is not strictly required to be changed for a mocked submit if we bypass it, but it has a default of '' which might fail HTML5 validation.
    // Let's submit the form directly.
    fireEvent.submit(screen.getByRole('button', { name: /Register Restaurant/i }).closest('form')!)

    expect(await screen.findByText('Email taken')).toBeInTheDocument()
    expect(mockNavigate).not.toHaveBeenCalled()
  })
})
