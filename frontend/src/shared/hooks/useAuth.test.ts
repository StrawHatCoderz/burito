import { renderHook, act } from '@testing-library/react'
import { useAuth } from './useAuth'

const TOKEN_KEY = 'burito_token'

beforeEach(() => {
  localStorage.clear()
})

describe('useAuth', () => {
  it('is not authenticated when localStorage has no token', () => {
    const { result } = renderHook(() => useAuth())
    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.token).toBeNull()
  })

  it('is authenticated after login()', () => {
    const { result } = renderHook(() => useAuth())
    act(() => {
      result.current.login('test-token')
    })
    expect(result.current.isAuthenticated).toBe(true)
    expect(result.current.token).toBe('test-token')
    expect(localStorage.getItem(TOKEN_KEY)).toBe('test-token')
  })

  it('is not authenticated after logout()', () => {
    const { result } = renderHook(() => useAuth())
    act(() => {
      result.current.login('test-token')
    })
    act(() => {
      result.current.logout()
    })
    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.token).toBeNull()
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull()
  })

  it('reads existing token from localStorage on mount', () => {
    localStorage.setItem(TOKEN_KEY, 'existing-token')
    const { result } = renderHook(() => useAuth())
    expect(result.current.isAuthenticated).toBe(true)
    expect(result.current.token).toBe('existing-token')
  })

  it('is not authenticated when login() is called with empty string', () => {
    const { result } = renderHook(() => useAuth())
    act(() => {
      result.current.login('')
    })
    expect(result.current.isAuthenticated).toBe(false)
  })

  it('logout() is safe when already logged out', () => {
    const { result } = renderHook(() => useAuth())
    expect(() => act(() => result.current.logout())).not.toThrow()
    expect(result.current.isAuthenticated).toBe(false)
  })
})
