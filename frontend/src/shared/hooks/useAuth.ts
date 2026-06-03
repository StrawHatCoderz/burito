import { useState, useCallback } from 'react'

const TOKEN_KEY = 'burito_token'

export function useAuth() {
  const [token, setToken] = useState<string | null>(() =>
    localStorage.getItem(TOKEN_KEY),
  )

  const login = useCallback((newToken: string) => {
    localStorage.setItem(TOKEN_KEY, newToken)
    setToken(newToken)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    setToken(null)
  }, [])

  return {
    token,
    isAuthenticated: token !== null && token.length > 0,
    login,
    logout,
  }
}
