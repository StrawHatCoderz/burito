import client from '../../../shared/api/client'

export const checkoutCart = async () => {
  const { data } = await client.post('/orders/checkout')
  return data
}

export const getActiveOrder = async () => {
  const { data } = await client.get('/orders/active')
  return data
}
