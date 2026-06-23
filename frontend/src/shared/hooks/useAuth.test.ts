import { renderHook, act } from '@testing-library/react'
import React from 'react'
import { useAuth } from './useAuth'
import { AuthProvider } from '../context/AuthContext'
import { vi } from 'vitest'

vi.mock('jwt-decode', () => ({
  jwtDecode: vi.fn((token: string) => {
    if (token === 'invalid-token') throw new Error('Invalid token')
    if (token === 'admin-token') return { role: 'RESTAURANT_ADMIN', restaurantId: '123' }
    if (token === 'customer-token') return { role: 'ROLE_CUSTOMER' }
    return {}
  })
}))

const TOKEN_KEY = 'burito_token'

const wrapper = ({ children }: { children: React.ReactNode }) =>
  React.createElement(AuthProvider, null, children)

beforeEach(() => {
  localStorage.clear()
  vi.clearAllMocks()
})

describe('useAuth', () => {
  it('is not authenticated when localStorage has no token', () => {
    const { result } = renderHook(() => useAuth(), { wrapper })
    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.token).toBeNull()
  })

  it('is authenticated after login()', () => {
    const { result } = renderHook(() => useAuth(), { wrapper })
    act(() => {
      result.current.login('customer-token')
    })
    expect(result.current.isAuthenticated).toBe(true)
    expect(result.current.token).toBe('customer-token')
    expect(localStorage.getItem(TOKEN_KEY)).toBe('customer-token')
    expect(result.current.role).toBe('ROLE_CUSTOMER')
  })

  it('is not authenticated after logout()', () => {
    const { result } = renderHook(() => useAuth(), { wrapper })
    act(() => {
      result.current.login('customer-token')
    })
    act(() => {
      result.current.logout()
    })
    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.token).toBeNull()
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull()
  })

  it('reads existing token from localStorage on mount and decodes it', () => {
    localStorage.setItem(TOKEN_KEY, 'admin-token')
    const { result } = renderHook(() => useAuth(), { wrapper })
    expect(result.current.isAuthenticated).toBe(true)
    expect(result.current.token).toBe('admin-token')
    expect(result.current.role).toBe('RESTAURANT_ADMIN')
    expect(result.current.restaurantId).toBe('123')
  })

  it('login() as admin succeeds with correct role', () => {
    const { result } = renderHook(() => useAuth(), { wrapper })
    act(() => {
      result.current.login('admin-token', true)
    })
    expect(result.current.isAuthenticated).toBe(true)
    expect(result.current.role).toBe('RESTAURANT_ADMIN')
  })

  it('login() as admin fails with incorrect role', () => {
    const { result } = renderHook(() => useAuth(), { wrapper })
    expect(() => {
      act(() => {
        result.current.login('customer-token', true)
      })
    }).toThrow('Unauthorized: Admin role required')
    expect(result.current.isAuthenticated).toBe(false)
  })

  it('login() throws when token is invalid', () => {
    const { result } = renderHook(() => useAuth(), { wrapper })
    expect(() => {
      act(() => {
        result.current.login('invalid-token')
      })
    }).toThrow()
    expect(result.current.isAuthenticated).toBe(false)
  })

  it('logout() is safe when already logged out', () => {
    const { result } = renderHook(() => useAuth(), { wrapper })
    expect(() => act(() => result.current.logout())).not.toThrow()
    expect(result.current.isAuthenticated).toBe(false)
  })
})

