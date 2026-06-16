import { useState, type FormEvent } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import TextField from '@mui/material/TextField'
import Button from '@mui/material/Button'
import Alert from '@mui/material/Alert'
import InputAdornment from '@mui/material/InputAdornment'
import * as authApi from '../../shared/api/authApi'
import { useAuth } from '../../shared/hooks/useAuth'
import { extractErrorMessage } from '../../shared/api/types'

export const LoginPage = () => {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setErrorMessage('')
    setIsSubmitting(true)

    try {
      const responseBody = await authApi.login({ email, password })
      login(responseBody.data.accessToken)
      navigate('/restaurants')
    } catch (error) {
      setErrorMessage(extractErrorMessage(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="bg-white rounded-2xl shadow-md p-8 w-full max-w-sm">
        <h1 className="text-2xl font-bold text-gray-800 mb-6">Sign in</h1>

        {errorMessage && (
          <Alert severity="error" className="mb-4">
            {errorMessage}
          </Alert>
        )}

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <TextField
            label="Email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            fullWidth
          />
          <TextField
            label="Password"
            type={showPassword ? 'text' : 'password'}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            fullWidth
            slotProps={{
              input: {
                endAdornment: (
                  <InputAdornment position="end">
                    <Button
                      size="small"
                      variant="text"
                      onClick={() => setShowPassword((v) => !v)}
                      tabIndex={-1}
                    >
                      {showPassword ? 'Hide' : 'Show'}
                    </Button>
                  </InputAdornment>
                ),
              },
            }}
          />
          <Button
            type="submit"
            variant="contained"
            disabled={isSubmitting}
            fullWidth
          >
            {isSubmitting ? 'Signing in…' : 'Sign in'}
          </Button>
        </form>

        <p className="text-sm text-gray-500 mt-4 text-center">
          No account?{' '}
          <Link to="/register" className="text-blue-600 hover:underline">
            Register
          </Link>
        </p>
      </div>
    </div>
  )
}
