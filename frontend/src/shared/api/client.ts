import axios from 'axios'

const TOKEN_KEY = 'burito_token'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  } else {
    let guestId = localStorage.getItem('guest_id')
    if (!guestId) {
      guestId = crypto.randomUUID()
      localStorage.setItem('guest_id', guestId)
    }
    config.headers['X-Guest-Id'] = guestId
  }
  return config
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthEndpoint = error.config?.url?.includes('/auth/')
    if (error.response?.status === 401 && !isAuthEndpoint) {
      localStorage.removeItem(TOKEN_KEY)
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

export default client
