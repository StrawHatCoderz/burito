import client from '../../shared/api/client'
import type { ApiResponse } from '../../shared/api/types'
import type { Restaurant } from '../catalog/types'

export interface UpdateRestaurantPayload {
  restaurantName?: string
  cuisineType?: string
  estDeliveryMinutes?: number
  open?: boolean
  imageUrl?: string | null
}

export const getAdminRestaurant = async (id: string): Promise<Restaurant> => {
  const { data } = await client.get<ApiResponse<Restaurant>>(`/admin/restaurants/${id}`)
  return data.data!
}

export const updateAdminRestaurant = async (
  id: string,
  payload: UpdateRestaurantPayload
): Promise<Restaurant> => {
  const { data } = await client.put<ApiResponse<Restaurant>>(`/admin/restaurants/${id}`, payload)
  return data.data!
}
