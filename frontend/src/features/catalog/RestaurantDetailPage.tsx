import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import CircularProgress from '@mui/material/CircularProgress'
import Divider from '@mui/material/Divider'
import Typography from '@mui/material/Typography'
import { fetchRestaurantWithMenu } from './catalogApi'
import { MenuSection } from './MenuSection'
import type { RestaurantWithMenu } from './types'

const CATEGORY_ORDER = ['STARTERS', 'MAINS', 'SIDES', 'DESSERTS', 'BEVERAGES']

type Status = 'loading' | 'success' | 'not-found' | 'error'

const is404 = (error: unknown) =>
  (error as { response?: { status?: number } }).response?.status === 404

export const RestaurantDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [status, setStatus] = useState<Status>('loading')
  const [data, setData] = useState<RestaurantWithMenu | null>(null)

  useEffect(() => {
    if (!id) return
    fetchRestaurantWithMenu(id)
      .then((result) => {
        setData(result)
        setStatus('success')
      })
      .catch((error) => setStatus(is404(error) ? 'not-found' : 'error'))
  }, [id])

  if (status === 'loading') {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <CircularProgress />
      </div>
    )
  }

  if (status === 'not-found') {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center gap-4 p-4">
        <Typography variant="h5" fontWeight={600}>
          Restaurant not found
        </Typography>
        <Button variant="outlined" onClick={() => navigate('/restaurants')}>
          Back to restaurants
        </Button>
      </div>
    )
  }

  if (status === 'error') {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center gap-4 p-4">
        <Alert severity="error">Something went wrong. Please try again.</Alert>
        <Button variant="outlined" onClick={() => navigate('/restaurants')}>
          Back to restaurants
        </Button>
      </div>
    )
  }

  const { restaurant, menuItems } = data!

  const menuSections = CATEGORY_ORDER.map((cat) => ({
    category: cat,
    items: menuItems.filter((i) => i.category === cat),
  })).filter((s) => s.items.length > 0)

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-white px-4 py-6 md:px-8 shadow-sm">
        <Button size="small" onClick={() => navigate('/restaurants')} className="mb-2">
          ← Restaurants
        </Button>
        <div className="flex items-start justify-between gap-3 mt-2">
          <div>
            <Typography variant="h4" component="h1" fontWeight={700}>
              {restaurant.restaurantName}
            </Typography>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              {restaurant.cuisineType.replace(/_/g, ' ')}
            </Typography>
            <div className="flex items-center gap-2 text-sm text-gray-600 mt-1">
              <span>★ {restaurant.rating.toFixed(1)}</span>
              <span aria-hidden>·</span>
              <span>{restaurant.estDeliveryMinutes} min</span>
            </div>
          </div>
          <Chip
            label={restaurant.open ? 'Open' : 'Closed'}
            color={restaurant.open ? 'success' : 'default'}
          />
        </div>
      </div>

      <div className="px-4 py-6 md:px-8 max-w-2xl">
        <Typography variant="h5" component="h2" fontWeight={600} gutterBottom>
          Menu
        </Typography>
        <Divider sx={{ mb: 3 }} />

        {menuSections.length === 0 ? (
          <Typography color="text.secondary">No menu available yet</Typography>
        ) : (
          menuSections.map((section) => (
            <MenuSection key={section.category} category={section.category} items={section.items} />
          ))
        )}
      </div>
    </div>
  )
}
