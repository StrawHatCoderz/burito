import client from '../../shared/api/client'
import type { ApiResponse } from '../../shared/api/types'
import type { MenuItem, Restaurant, RestaurantWithMenu } from './types'

export const fetchRestaurants = async (): Promise<Restaurant[]> => {
  const { data } = await client.get<ApiResponse<Restaurant[]>>('/restaurants/')
  return data.data ?? []
}

export const fetchRestaurantWithMenu = async (id: string): Promise<RestaurantWithMenu> => {
  const [restaurantRes, menuRes] = await Promise.all([
    client.get<ApiResponse<Restaurant>>(`/restaurants/${id}`),
    client.get<ApiResponse<MenuItem[]>>(`/restaurants/${id}/menu`),
  ])
  return {
    restaurant: restaurantRes.data.data!,
    menuItems: menuRes.data.data ?? [],
  }
}
