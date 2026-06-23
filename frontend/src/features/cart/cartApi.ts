import client from '../../shared/api/client'
import type { ApiResponse } from '../../shared/api/types'
import type { CartView } from './types'

export const addToCart = async (
  menuItemId: string,
  quantity: number
): Promise<CartView> => {
  const { data } = await client.post<ApiResponse<CartView>>('/cart/items', {
    menuItemId,
    quantity,
  })
  return data.data!
}

export const decrementCartItem = async (cartItemId: string): Promise<CartView> => {
  const { data } = await client.put<ApiResponse<CartView>>(`/cart/items/${cartItemId}/decrement`)
  return data.data!
}

export const removeCartItem = async (cartItemId: string): Promise<CartView> => {
  const { data } = await client.delete<ApiResponse<CartView>>(`/cart/items/${cartItemId}`)
  return data.data!
}

export const clearCart = async (): Promise<CartView> => {
  const { data } = await client.delete<ApiResponse<CartView>>('/cart')
  return data.data!
}

export const mergeCart = async (): Promise<void> => {
  const guestId = localStorage.getItem('guest_id')
  if (guestId) {
    await client.post('/cart/merge', null, { headers: { 'X-Guest-Id': guestId } })
  }
}
