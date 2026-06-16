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
