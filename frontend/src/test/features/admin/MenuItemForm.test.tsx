import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { vi, describe, it, expect } from 'vitest'
import { MenuItemForm } from '../../../features/admin/MenuItemForm'

describe('MenuItemForm', () => {
  const mockOnSubmit = vi.fn()
  const mockOnClose = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders Add Item form correctly', () => {
    render(<MenuItemForm open={true} onClose={mockOnClose} onSubmit={mockOnSubmit} />)
    expect(screen.getByText('Add New Item')).toBeInTheDocument()
    expect(screen.getByLabelText(/Item Name/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/Price/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/Image URL/i)).toBeInTheDocument()
  })

  it('renders Edit Item form correctly with initial data', () => {
    const initialData = {
      menuItemId: '1',
      name: 'Test Taco',
      description: 'A good taco',
      price: 5.99,
      category: 'MAINS',
      available: true,
      imageUrl: 'http://test.com/taco.png',
    }

    render(
      <MenuItemForm 
        open={true} 
        onClose={mockOnClose} 
        onSubmit={mockOnSubmit} 
        initialData={initialData} 
      />
    )

    expect(screen.getByText('Edit Item')).toBeInTheDocument()
    expect(screen.getByDisplayValue('Test Taco')).toBeInTheDocument()
    expect(screen.getByDisplayValue('5.99')).toBeInTheDocument()
  })

  it('calls onSubmit with correct data', async () => {
    render(<MenuItemForm open={true} onClose={mockOnClose} onSubmit={mockOnSubmit} />)

    fireEvent.change(screen.getByLabelText(/Item Name/i), { target: { value: 'New Taco' } })
    fireEvent.change(screen.getByLabelText(/Price/i), { target: { value: '9.99' } })

    fireEvent.click(screen.getByRole('button', { name: /Add Item/i }))

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith(expect.objectContaining({
        name: 'New Taco',
        price: 9.99,
        category: 'MAINS',
        isAvailable: true,
        description: null,
        imageUrl: null,
      }))
    })
  })
})
