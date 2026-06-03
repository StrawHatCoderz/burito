import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Alert from '@mui/material/Alert'
import Card from '@mui/material/Card'
import CardActionArea from '@mui/material/CardActionArea'
import CardContent from '@mui/material/CardContent'
import Chip from '@mui/material/Chip'
import CircularProgress from '@mui/material/CircularProgress'
import Typography from '@mui/material/Typography'
import { fetchRestaurants } from './catalogApi'
import type { Restaurant } from './types'

type Status = 'loading' | 'success' | 'error'

export const RestaurantsPage = () => {
  const navigate = useNavigate()
  const [status, setStatus] = useState<Status>('loading')
  const [restaurants, setRestaurants] = useState<Restaurant[]>([])

  useEffect(() => {
    fetchRestaurants()
      .then((data) => {
        setRestaurants(data)
        setStatus('success')
      })
      .catch(() => setStatus('error'))
  }, [])

  if (status === 'loading') {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <CircularProgress />
      </div>
    )
  }

  if (status === 'error') {
    return (
      <div className="min-h-screen flex items-center justify-center p-4">
        <Alert severity="error">Failed to load restaurants. Please try again.</Alert>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 p-4 md:p-8">
      <Typography variant="h4" component="h1" gutterBottom fontWeight={700}>
        Restaurants
      </Typography>

      {restaurants.length === 0 ? (
        <Typography color="text.secondary">No restaurants available right now</Typography>
      ) : (
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
