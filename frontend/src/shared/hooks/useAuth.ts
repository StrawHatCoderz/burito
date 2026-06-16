import { useState, useCallback } from 'react'
import { jwtDecode } from 'jwt-decode'

const TOKEN_KEY = 'burito_token'

export function useAuth() {
  const [token, setToken] = useState<string | null>(() =>
    localStorage.getItem(TOKEN_KEY),
  )

  let decodedToken: any = null
  try {
    if (token) {
      decodedToken = jwtDecode<any>(token)
    }
  } catch (e) {
    // ignore invalid token in localstorage on initial load, but ideally we'd clear it
  }

  const role = decodedToken?.role || null
  const restaurantId = decodedToken?.restaurantId || null

  const login = useCallback((newToken: string, isAdminLogin: boolean = false) => {
    try {
      const decoded = jwtDecode<any>(newToken)
      if (isAdminLogin && decoded.role !== 'ROLE_RESTAURANT_ADMIN') {
        throw new Error('Unauthorized: Admin role required')
      }
      localStorage.setItem(TOKEN_KEY, newToken)
      setToken(newToken)
    } catch (error) {
      localStorage.removeItem(TOKEN_KEY)
      setToken(null)
      throw error
    }
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    setToken(null)
  }, [])

  return {
    token,
    isAuthenticated: token !== null && token.length > 0,
    role,
    restaurantId,
    login,
    logout,
  }
}
