import client from '../../../shared/api/client'
import type { ApiResponse } from '../../../shared/api/types'

export const checkoutCart = async () => {
  const { data } = await client.post<ApiResponse<any>>('/orders/checkout')
  return data.data
}

export const getActiveOrder = async () => {
  const { data } = await client.get<ApiResponse<any>>('/orders/active')
  return data.data
}
