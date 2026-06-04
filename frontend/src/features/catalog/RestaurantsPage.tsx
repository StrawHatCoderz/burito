import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Alert from '@mui/material/Alert'
import Card from '@mui/material/Card'
import CardActionArea from '@mui/material/CardActionArea'
import CardContent from '@mui/material/CardContent'
import Chip from '@mui/material/Chip'
import CircularProgress from '@mui/material/CircularProgress'
import FormControl from '@mui/material/FormControl'
import InputLabel from '@mui/material/InputLabel'
import MenuItem from '@mui/material/MenuItem'
import Select from '@mui/material/Select'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { fetchRestaurants } from './catalogApi'
import { useDebounce } from '../../shared/hooks/useDebounce'
import type { Restaurant } from './types'

const CUISINES = [
  'AMERICAN', 'CHINESE', 'INDIAN', 'ITALIAN', 'JAPANESE',
  'KOREAN', 'LEBANESE', 'MEDITERRANEAN', 'MEXICAN', 'SOUTH_INDIAN', 'THAI',
]

type Status = 'loading' | 'success' | 'error'

export const RestaurantsPage = () => {
  const navigate = useNavigate()
  const [status, setStatus] = useState<Status>('loading')
  const [restaurants, setRestaurants] = useState<Restaurant[]>([])
  const [searchInput, setSearchInput] = useState('')
  const [cuisineFilter, setCuisineFilter] = useState('')
  const searchDebounced = useDebounce(searchInput, 300)

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
    <div className="min-h-screen bg-gray-50 p-4 md:p-8">
      <Typography variant="h4" component="h1" gutterBottom fontWeight={700}>
        Restaurants
      </Typography>

      <div className="flex flex-col gap-3 mb-6 sm:flex-row">
        <TextField
          label="Search restaurants"
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          size="small"
          fullWidth
        />
        <FormControl size="small" sx={{ minWidth: 180 }}>
          <InputLabel>Cuisine</InputLabel>
          <Select
            value={cuisineFilter}
            label="Cuisine"
            onChange={(e) => setCuisineFilter(e.target.value)}
          >
            <MenuItem value="">All cuisines</MenuItem>
            {CUISINES.map((c) => (
              <MenuItem key={c} value={c}>
                {c.replace(/_/g, ' ')}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </div>

      {status === 'loading' && (
        <div className="flex justify-center py-12">
          <CircularProgress />
        </div>
      )}

      {status === 'error' && (
        <Alert severity="error">Failed to load restaurants. Please try again.</Alert>
      )}

      {status === 'success' && restaurants.length === 0 && (
        <Typography color="text.secondary">No restaurants found</Typography>
      )}

      {status === 'success' && restaurants.length > 0 && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {restaurants.map((r) => (
            <Card key={r.restaurantId}>
              <CardActionArea onClick={() => navigate(`/restaurants/${r.restaurantId}`)}>
                <CardContent>
                  <div className="flex items-start justify-between gap-2 mb-1">
                    <Typography variant="h6" component="h2" fontWeight={600} lineHeight={1.3}>
                      {r.restaurantName}
                    </Typography>
                    <Chip
                      label={r.open ? 'Open' : 'Closed'}
                      size="small"
                      color={r.open ? 'success' : 'default'}
                    />
                  </div>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    {r.cuisineType.replace(/_/g, ' ')}
                  </Typography>
                  <div className="flex items-center gap-2 text-sm text-gray-600 mt-2">
                    <span>★ {r.rating.toFixed(1)}</span>
                    <span aria-hidden>·</span>
                    <span>{r.estDeliveryMinutes} min</span>
                  </div>
                </CardContent>
              </CardActionArea>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
