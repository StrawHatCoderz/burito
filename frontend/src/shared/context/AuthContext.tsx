import { createContext, useContext, useState, useCallback, type ReactNode } from 'react'
import { jwtDecode } from 'jwt-decode'

const TOKEN_KEY = 'burito_token'

interface AuthContextValue {
  token: string | null
  isAuthenticated: boolean
  role: string | null
  restaurantId: string | null
  login: (newToken: string, isAdminLogin?: boolean) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [token, setToken] = useState<string | null>(() =>
    localStorage.getItem(TOKEN_KEY),
  )

  let decodedToken: any = null
  try {
    if (token) {
      decodedToken = jwtDecode<any>(token)
    }
  } catch (e) {
    // ignore invalid token
  }

  const role = decodedToken?.role || null
  const restaurantId = decodedToken?.restaurantId || null

  const login = useCallback((newToken: string, isAdminLogin: boolean = false) => {
    try {
      const decoded = jwtDecode<any>(newToken)
      if (isAdminLogin && decoded.role !== 'RESTAURANT_ADMIN') {
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

  return (
    <AuthContext.Provider
      value={{
        token,
        isAuthenticated: token !== null && token.length > 0,
        role,
        restaurantId,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export const useAuthContext = (): AuthContextValue => {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuthContext must be used within an AuthProvider')
  }
  return ctx
}
