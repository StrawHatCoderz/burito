import client from '../../shared/api/client'
import type { ApiResponse } from '../../shared/api/types'
import type { MenuItem, Restaurant, RestaurantWithMenu } from './types'

export interface RestaurantSearchParams {
  search?: string
  cuisine?: string
}

export const fetchRestaurants = async (params?: RestaurantSearchParams): Promise<Restaurant[]> => {
  const query: Record<string, string> = {}
  if (params?.search) query.search = params.search
  if (params?.cuisine) query.cuisine = params.cuisine
  const { data } = await client.get<ApiResponse<Restaurant[]>>('/restaurants/', { params: query })
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
