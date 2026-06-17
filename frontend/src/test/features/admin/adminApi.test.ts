import { describe, it, expect, vi } from 'vitest'
import client from '../../../features/../shared/api/client'
import { getAdminRestaurant, updateAdminRestaurant } from '../../../features/admin/adminApi'
import type { Restaurant } from '../../../features/catalog/types'

vi.mock('../../../features/../shared/api/client', () => ({
  default: {
    get: vi.fn(),
    put: vi.fn(),
  },
}))

describe('adminApi', () => {
  const mockRestaurant: Restaurant = {
    restaurantId: '123',
    restaurantName: 'Test Name',
    cuisineType: 'ITALIAN',
    rating: 4.5,
    estDeliveryMinutes: 30,
    open: true,
    imageUrl: 'http://test.com/image.jpg',
    description: null,
    createdAt: '2023-01-01',
    address: null,
  }

  it('getAdminRestaurant calls GET with correct URL', async () => {
    vi.mocked(client.get).mockResolvedValue({ data: { data: mockRestaurant } })
    const res = await getAdminRestaurant('123')
    expect(client.get).toHaveBeenCalledWith('/admin/restaurants/123')
    expect(res).toEqual(mockRestaurant)
  })

  it('updateAdminRestaurant calls PUT with correct URL and payload', async () => {
    vi.mocked(client.put).mockResolvedValue({ data: { data: mockRestaurant } })
    const payload = { open: false }
    const res = await updateAdminRestaurant('123', payload)
    expect(client.put).toHaveBeenCalledWith('/admin/restaurants/123', payload)
    expect(res).toEqual(mockRestaurant)
  })
})
