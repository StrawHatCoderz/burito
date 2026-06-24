import client from '../../shared/api/client'
import type { ApiResponse } from '../../shared/api/types'

export const getActiveOrders = async () => {
  const { data } = await client.get<ApiResponse<any[]>>('/admin/orders')
  return data.data ?? []
}

export const updateOrderStatus = async (orderId: string, status: string) => {
  const { data } = await client.put<ApiResponse<any>>(`/admin/orders/${orderId}/status`, { status })
  return data.data
}
