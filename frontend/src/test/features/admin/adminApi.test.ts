import { describe, it, expect, vi } from 'vitest'
import client from '../../../features/../shared/api/client'
import { getAdminRestaurant, updateAdminRestaurant } from '../../../features/admin/adminApi'
import type { Restaurant } from '../../../features/catalog/types'

vi.mock('../../../features/../shared/api/client', () => ({
  default: {
    get: vi.fn(),
    put: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
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
    vi.mocked(client.get).mockResolvedValue({ data: { success: true, data: mockRestaurant, error: null } })
    const res = await getAdminRestaurant('123')
    expect(client.get).toHaveBeenCalledWith('/admin/restaurants/123')
    expect(res).toEqual(mockRestaurant)
  })

  it('updateAdminRestaurant calls PUT with correct URL and payload', async () => {
    vi.mocked(client.put).mockResolvedValue({ data: { success: true, data: mockRestaurant, error: null } })
    const payload = { open: false }
    const res = await updateAdminRestaurant('123', payload)
    expect(client.put).toHaveBeenCalledWith('/admin/restaurants/123', payload)
    expect(res).toEqual(mockRestaurant)
  })

  describe('Menu operations', () => {
    const mockRestaurantId = 'rest-123'
    const mockMenuItemId = 'item-456'

    it('should fetch the menu for a restaurant', async () => {
      const mockMenu = [{ menuItemId: '1', name: 'Tacos' }]
      vi.mocked(client.get).mockResolvedValueOnce({ data: { data: mockMenu } })

      const { getAdminMenu } = await import('../../../features/admin/adminApi')
      const result = await getAdminMenu(mockRestaurantId)
      
      expect(client.get).toHaveBeenCalledWith(`/restaurants/${mockRestaurantId}/menu`)
      expect(result).toEqual(mockMenu)
    })

    it('should add a menu item', async () => {
      const payload = { name: 'Tacos', description: null, price: 10, category: 'MAINS', isAvailable: true, imageUrl: null }
      const mockResponse = { menuItemId: '1', ...payload }
      vi.mocked(client.post).mockResolvedValueOnce({ data: { success: true, data: mockResponse, error: null } })

      const { addMenuItem } = await import('../../../features/admin/adminApi')
      const result = await addMenuItem(mockRestaurantId, payload)
      
      expect(client.post).toHaveBeenCalledWith(`/admin/restaurants/${mockRestaurantId}/menu`, payload)
      expect(result).toEqual(mockResponse)
    })

    it('should update a menu item', async () => {
      const payload = { name: 'Burritos', description: null, price: 12, category: 'MAINS', isAvailable: true, imageUrl: null }
      const mockResponse = { menuItemId: mockMenuItemId, ...payload }
      vi.mocked(client.put).mockResolvedValueOnce({ data: { success: true, data: mockResponse, error: null } })

      const { updateMenuItem } = await import('../../../features/admin/adminApi')
      const result = await updateMenuItem(mockRestaurantId, mockMenuItemId, payload)
      
      expect(client.put).toHaveBeenCalledWith(`/admin/restaurants/${mockRestaurantId}/menu/${mockMenuItemId}`, payload)
      expect(result).toEqual(mockResponse)
    })

    it('should delete a menu item', async () => {
      vi.mocked(client.delete).mockResolvedValueOnce({ data: {} })

      const { deleteMenuItem } = await import('../../../features/admin/adminApi')
      await deleteMenuItem(mockRestaurantId, mockMenuItemId)
      
      expect(client.delete).toHaveBeenCalledWith(`/admin/restaurants/${mockRestaurantId}/menu/${mockMenuItemId}`)
    })
  })
})
