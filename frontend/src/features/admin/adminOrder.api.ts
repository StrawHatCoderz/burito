import client from '../../shared/api/client'

export const getActiveOrders = async () => {
  const { data } = await client.get('/admin/orders')
  return data
}

export const updateOrderStatus = async (orderId: string, status: string) => {
  const { data } = await client.put(`/admin/orders/${orderId}/status`, { status })
  return data
}
