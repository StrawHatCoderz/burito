import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Skeleton from '@mui/material/Skeleton'
import Typography from '@mui/material/Typography'
import InputBase from '@mui/material/InputBase'
import IconButton from '@mui/material/IconButton'
import Paper from '@mui/material/Paper'
import { PromoCarousel } from './PromoCarousel'
import { fetchRestaurants } from './catalogApi'
import { useDebounce } from '../../shared/hooks/useDebounce'
import { useAuth } from '../../shared/hooks/useAuth'
import type { Restaurant } from './types'

const CUISINES = [
  { id: '', label: 'All', icon: '🍽️' },
  { id: 'AMERICAN', label: 'American', icon: '🍔' },
  { id: 'CHINESE', label: 'Chinese', icon: '🍜' },
  { id: 'INDIAN', label: 'Indian', icon: '🍛' },
  { id: 'ITALIAN', label: 'Italian', icon: '🍕' },
  { id: 'JAPANESE', label: 'Japanese', icon: '🍣' },
  { id: 'KOREAN', label: 'Korean', icon: '🍱' },
  { id: 'LEBANESE', label: 'Lebanese', icon: '🧆' },
  { id: 'MEDITERRANEAN', label: 'Mediterranean', icon: '🥙' },
  { id: 'MEXICAN', label: 'Mexican', icon: '🌮' },
  { id: 'SOUTH_INDIAN', label: 'South Indian', icon: '🥞' },
  { id: 'THAI', label: 'Thai', icon: '🥘' },
]

const getGradientForCuisine = (cuisine: string) => {
  const gradients: Record<string, string> = {
    AMERICAN: 'from-blue-400 to-indigo-500',
    CHINESE: 'from-red-400 to-red-600',
    INDIAN: 'from-orange-400 to-orange-600',
    ITALIAN: 'from-green-400 to-emerald-600',
    JAPANESE: 'from-rose-400 to-pink-500',
    KOREAN: 'from-purple-400 to-purple-600',
    MEXICAN: 'from-yellow-400 to-orange-500',
    SOUTH_INDIAN: 'from-amber-400 to-yellow-600',
    THAI: 'from-lime-400 to-green-600',
  }
  return gradients[cuisine] || 'from-gray-400 to-gray-500'
}

type Status = 'loading' | 'success' | 'error'

export const RestaurantsPage = () => {
  const navigate = useNavigate()
  const { token } = useAuth()
  const [status, setStatus] = useState<Status>('loading')
  const [restaurants, setRestaurants] = useState<Restaurant[]>([])
  const [searchInput, setSearchInput] = useState('')
  const [cuisineFilter, setCuisineFilter] = useState('')
  const searchDebounced = useDebounce(searchInput, 300)

  let userName = ''
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      if (payload.full_name) {
        userName = payload.full_name.split(' ')[0]
      } else if (payload.email) {
        userName = payload.email.split('@')[0]
      }
    } catch(e) {}
  }

  const greeting = userName ? `Hungry, ${userName}? 👋` : 'Hungry? 👋'

  useEffect(() => {
    setStatus('loading')
    fetchRestaurants({
      search: searchDebounced || undefined,
      cuisine: cuisineFilter || undefined,
    })
      .then((data) => {
        setRestaurants(data)
        setStatus('success')
      })
      .catch(() => setStatus('error'))
  }, [searchDebounced, cuisineFilter])

  return (
    <div className="min-h-screen bg-[#FFF9F5] p-4 md:p-8">
      <div className="max-w-7xl mx-auto">
        
        {/* Page Header */}
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-4 mb-8">
          <div>
            <Typography variant="h4" component="h1" fontWeight={800} color="textPrimary" sx={{ letterSpacing: '-0.02em' }}>
              {greeting}
            </Typography>
            <Typography variant="body1" color="textSecondary" sx={{ mt: 0.5, fontWeight: 500 }}>
              Let's find something delicious for you.
            </Typography>
          </div>
          
          <div className="w-full md:w-[400px]">
            <Paper
              component="form"
              sx={{ p: '2px 8px', display: 'flex', alignItems: 'center', width: '100%', borderRadius: '9999px', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.05)', border: '1px solid #e5e7eb', transition: 'box-shadow 0.2s', '&:focus-within': { boxShadow: '0 0 0 2px #FF5A5F33, 0 4px 6px -1px rgba(0, 0, 0, 0.05)' } }}
              onSubmit={(e) => e.preventDefault()}
            >
              <IconButton sx={{ p: '10px' }} aria-label="search" disabled>
                <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor" className="text-gray-400">
                  <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
                </svg>
              </IconButton>
              <InputBase
                sx={{ ml: 1, flex: 1, fontSize: '1rem' }}
                placeholder="Search restaurants, cuisines..."
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
              />
            </Paper>
          </div>
        </div>

        <PromoCarousel />

        {/* Cuisine Pills Navigation */}
        <div className="mb-8 overflow-x-auto pb-4 hide-scrollbar">
          <div className="flex gap-3 px-1">
            {CUISINES.map((c) => {
              const isActive = cuisineFilter === c.id
              return (
                <button
                  key={c.id}
                  onClick={() => setCuisineFilter(c.id)}
                  className={`flex items-center gap-2 px-5 py-2.5 rounded-full font-medium whitespace-nowrap transition-all duration-200 ${
                    isActive 
                      ? 'bg-[#FF5A5F] text-white shadow-md transform -translate-y-0.5' 
                      : 'bg-white text-gray-700 hover:bg-gray-50 border border-gray-200 hover:border-gray-300 shadow-sm'
                  }`}
                >
                  <span className="text-lg">{c.icon}</span>
                  {c.label}
                </button>
              )
            })}
          </div>
        </div>

        {/* Loading Skeletons */}
        {status === 'loading' && (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {[...Array(8)].map((_, i) => (
              <div key={i} className="bg-white rounded-2xl md:rounded-lg overflow-hidden shadow-sm border border-gray-100 flex flex-col h-[280px]">
                <Skeleton variant="rectangular" height={128} animation="wave" />
                <div className="p-5 flex flex-col flex-1">
                  <Skeleton variant="text" height={32} width="70%" />
                  <Skeleton variant="text" height={20} width="40%" className="mb-4" />
                  <div className="mt-auto flex gap-4">
                    <Skeleton variant="rounded" width={60} height={24} />
                    <Skeleton variant="rounded" width={80} height={24} />
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Error State */}
        {status === 'error' && (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <div className="w-20 h-20 rounded-full bg-red-100 flex items-center justify-center text-red-500 mb-4">
              <svg viewBox="0 0 24 24" width="40" height="40" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
              </svg>
            </div>
            <Typography variant="h6" fontWeight={700} gutterBottom>
              Oops! Something went wrong.
            </Typography>
            <Typography variant="body1" color="textSecondary">
              We couldn't load the restaurants. Please try refreshing the page.
            </Typography>
          </div>
        )}

        {/* Empty State */}
        {status === 'success' && restaurants.length === 0 && (
          <div className="flex flex-col items-center justify-center py-20 text-center bg-white rounded-3xl md:rounded-xl border border-gray-100 shadow-sm">
            <div className="text-6xl mb-4">🔍</div>
            <Typography variant="h5" fontWeight={700} gutterBottom color="textPrimary">
              No restaurants found
            </Typography>
            <Typography variant="body1" color="textSecondary" className="max-w-md">
              We couldn't find any restaurants matching your current search or cuisine filters. Try adjusting them!
            </Typography>
            <button 
              onClick={() => { setSearchInput(''); setCuisineFilter(''); }}
              className="mt-6 px-6 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-800 rounded-full font-semibold transition-colors"
            >
              Clear all filters
            </button>
          </div>
        )}

        {/* Restaurant Grid */}
        {status === 'success' && restaurants.length > 0 && (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {restaurants.map((r) => (
              <div 
                key={r.restaurantId}
                onClick={() => navigate(`/restaurants/${r.restaurantId}`)}
                className="group bg-white rounded-2xl md:rounded-lg overflow-hidden shadow-sm hover:shadow-xl transition-all duration-300 cursor-pointer border border-gray-100 flex flex-col hover:-translate-y-1 h-full"
              >
                {/* Abstract Background Header */}
                <div className={`h-32 w-full ${r.imageUrl ? 'bg-gray-200' : `bg-gradient-to-br ${getGradientForCuisine(r.cuisineType)}`} relative overflow-hidden`}>
                  {r.imageUrl ? (
                    <img src={r.imageUrl} alt={r.restaurantName} className="absolute inset-0 w-full h-full object-cover" />
                  ) : (
                    <div className="absolute inset-0 opacity-10" style={{ backgroundImage: 'radial-gradient(circle at 2px 2px, white 1px, transparent 0)', backgroundSize: '16px 16px' }}></div>
                  )}
                  
                  {/* Floating Delivery Time Badge */}
                  <div className="absolute bottom-3 left-3 bg-white/95 backdrop-blur-sm px-2.5 py-1 rounded-lg text-xs font-bold text-gray-800 shadow-sm flex items-center gap-1.5">
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor" className="text-gray-500">
                      <path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/>
                    </svg>
                    {r.estDeliveryMinutes} min
                  </div>
                  
                  {/* Status Badge */}
                  <div className={`absolute top-3 right-3 px-2.5 py-1 rounded-lg text-xs font-bold shadow-sm backdrop-blur-sm ${r.open ? 'bg-white/95 text-emerald-600' : 'bg-gray-900/95 text-white'}`}>
                    {r.open ? 'OPEN' : 'CLOSED'}
                  </div>
                </div>

                {/* Card Content */}
                <div className="p-5 flex flex-col flex-1">
                  <div className="flex justify-between items-start gap-2 mb-1.5">
                    <Typography variant="h6" component="h2" fontWeight={700} lineHeight={1.2} className="text-gray-900 group-hover:text-[#FF5A5F] transition-colors line-clamp-1">
                      {r.restaurantName}
                    </Typography>
                    <div className="flex items-center gap-1 bg-emerald-50 px-2 py-0.5 rounded-md shrink-0">
                      <span className="text-emerald-700 text-sm font-bold">{r.rating.toFixed(1)}</span>
                      <svg viewBox="0 0 24 24" width="12" height="12" fill="currentColor" className="text-emerald-500">
                        <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
                      </svg>
                    </div>
                  </div>
                  <Typography variant="body2" color="textSecondary" className="mb-4 font-medium flex items-center gap-1.5">
                    {r.cuisineType.replace(/_/g, ' ')}
                  </Typography>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
