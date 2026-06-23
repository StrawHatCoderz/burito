import axios from 'axios'

const TOKEN_KEY = 'burito_token'
const REFRESH_TOKEN_KEY = 'burito_refresh_token'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    if (config.headers && typeof config.headers.set === 'function') {
      config.headers.set('Authorization', `Bearer ${token}`)
    } else {
      config.headers.Authorization = `Bearer ${token}`
    }
  } else {
    let guestId = localStorage.getItem('guest_id')
    if (!guestId) {
      guestId = crypto.randomUUID()
      localStorage.setItem('guest_id', guestId)
    }
    if (config.headers && typeof config.headers.set === 'function') {
      config.headers.set('X-Guest-Id', guestId)
    } else {
      config.headers['X-Guest-Id'] = guestId
    }
  }
  return config
})

const setupResponseInterceptor = (axiosInstance: any) => {
  let isRefreshing = false
  let failedQueue: any[] = []

  const processQueue = (error: any, token: string | null = null) => {
    failedQueue.forEach((prom) => {
      if (error) {
        prom.reject(error)
      } else {
        prom.resolve(token)
      }
    })
    failedQueue = []
  }

  axiosInstance.interceptors.response.use(
    (response: any) => response,
    async (error: any) => {
      const originalRequest = error.config
      const isAuthEndpoint = originalRequest?.url?.includes('/auth/')
      
      if (error.response?.status === 401 && !isAuthEndpoint && !originalRequest._retry) {
        if (isRefreshing) {
          return new Promise(function (resolve, reject) {
            failedQueue.push({ resolve, reject })
          })
            .then((token) => {
              if (originalRequest.headers && typeof originalRequest.headers.set === 'function') {
                originalRequest.headers.set('Authorization', `Bearer ${token}`)
              } else {
                originalRequest.headers['Authorization'] = `Bearer ${token}`
              }
              return axiosInstance(originalRequest)
            })
            .catch((err) => Promise.reject(err))
        }

        originalRequest._retry = true
        isRefreshing = true

        const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)
        if (!refreshToken) {
          console.error('No refresh token available')
          localStorage.removeItem(TOKEN_KEY)
          if (window.location.pathname !== '/login' && !window.location.pathname.startsWith('/admin')) {
            window.location.href = '/login'
          }
          return Promise.reject(error)
        }

        try {
          const { data } = await axios.post((import.meta.env.VITE_API_BASE_URL ?? '/api') + '/auth/refresh', {
            refreshToken
          })
          
          const newAccessToken = data.data.accessToken
          const newRefreshToken = data.data.refreshToken
          
          localStorage.setItem(TOKEN_KEY, newAccessToken)
          localStorage.setItem(REFRESH_TOKEN_KEY, newRefreshToken)
          
          processQueue(null, newAccessToken)
          
          if (originalRequest.headers && typeof originalRequest.headers.set === 'function') {
            originalRequest.headers.set('Authorization', `Bearer ${newAccessToken}`)
          } else {
            originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`
          }
          return axiosInstance(originalRequest)
        } catch (refreshError) {
          processQueue(refreshError, null)
          localStorage.removeItem(TOKEN_KEY)
          localStorage.removeItem(REFRESH_TOKEN_KEY)
          if (window.location.pathname !== '/login' && !window.location.pathname.startsWith('/admin')) {
            window.location.href = '/login'
          }
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      }
      
      return Promise.reject(error)
    },
  )
}

setupResponseInterceptor(client)

export default client
