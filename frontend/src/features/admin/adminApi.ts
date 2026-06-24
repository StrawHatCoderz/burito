import client from '../../shared/api/client'
import type { ApiResponse } from '../../shared/api/types'
import type { Restaurant, MenuItem } from '../catalog/types'

export interface UpdateRestaurantPayload {
  restaurantName?: string
  cuisineType?: string
  estDeliveryMinutes?: number
  open?: boolean
  imageUrl?: string | null
}

export interface MenuItemPayload {
  name: string
  description: string | null
  price: number
  category: string
  isAvailable: boolean
  imageUrl: string | null
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

export const getAdminMenu = async (restaurantId: string): Promise<MenuItem[]> => {
  const { data } = await client.get<ApiResponse<MenuItem[]>>(`/restaurants/${restaurantId}/menu`)
  return data.data ?? []
}

export const addMenuItem = async (
  restaurantId: string,
  payload: MenuItemPayload
): Promise<MenuItem> => {
  const { data } = await client.post<ApiResponse<MenuItem>>(`/admin/restaurants/${restaurantId}/menu`, payload)
  return data.data!
}

export const updateMenuItem = async (
  restaurantId: string,
  menuItemId: string,
  payload: MenuItemPayload
): Promise<MenuItem> => {
  const { data } = await client.put<ApiResponse<MenuItem>>(`/admin/restaurants/${restaurantId}/menu/${menuItemId}`, payload)
  return data.data!
}

export const deleteMenuItem = async (
  restaurantId: string,
  menuItemId: string
): Promise<void> => {
  await client.delete(`/admin/restaurants/${restaurantId}/menu/${menuItemId}`)
}
