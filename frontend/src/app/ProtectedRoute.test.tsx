import { render, screen } from '@testing-library/react'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import { ProtectedRoute } from './ProtectedRoute'

const TOKEN_KEY = 'burito_token'

beforeEach(() => {
  localStorage.clear()
})

const renderWithRouter = (initialPath: string) =>
  render(
    <MemoryRouter initialEntries={[initialPath]}>
      <Routes>
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <div>Protected content</div>
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<div>Login page</div>} />
      </Routes>
    </MemoryRouter>,
  )

describe('ProtectedRoute', () => {
  it('renders children when the user is authenticated', () => {
    localStorage.setItem(TOKEN_KEY, 'valid-token')
    renderWithRouter('/')
    expect(screen.getByText('Protected content')).toBeInTheDocument()
  })

  it('redirects to /login when the user is not authenticated', () => {
    renderWithRouter('/')
    expect(screen.getByText('Login page')).toBeInTheDocument()
    expect(screen.queryByText('Protected content')).not.toBeInTheDocument()
  })
})
