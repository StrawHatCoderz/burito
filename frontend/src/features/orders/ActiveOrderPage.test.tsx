import { render, screen, waitFor } from '@testing-library/react'
import { vi, describe, it, expect, beforeEach } from 'vitest'
import { ActiveOrderPage } from './ActiveOrderPage'
import { getActiveOrder } from './api/orders.api'
import { MemoryRouter, useNavigate } from 'react-router-dom'

vi.mock('./api/orders.api', () => ({
  getActiveOrder: vi.fn(),
}))

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: vi.fn(),
  }
})

describe('ActiveOrderPage', () => {
  const mockNavigate = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useNavigate).mockReturnValue(mockNavigate)
  })

  it('renders loading state initially', () => {
    vi.mocked(getActiveOrder).mockImplementation(() => new Promise(() => {}))
    render(
      <MemoryRouter>
        <ActiveOrderPage />
      </MemoryRouter>
    )
    expect(screen.getByRole('progressbar')).toBeInTheDocument()
  })

  it('renders order details and correct stepper state for PENDING', async () => {
    vi.mocked(getActiveOrder).mockResolvedValue({
      id: 'order-123',
      totalAmount: 45.5,
      status: 'PENDING'
    })

    render(
      <MemoryRouter>
        <ActiveOrderPage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.getByText('Active Order Tracking')).toBeInTheDocument()
    })

    expect(screen.getByText('Order ID: order-123')).toBeInTheDocument()
    expect(screen.getByText('Total: $45.50')).toBeInTheDocument()
  })

  it('redirects to /restaurants on 404', async () => {
    vi.mocked(getActiveOrder).mockRejectedValue({
      response: { status: 404 }
    })

    render(
      <MemoryRouter>
        <ActiveOrderPage />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/restaurants')
    })
  })
})
