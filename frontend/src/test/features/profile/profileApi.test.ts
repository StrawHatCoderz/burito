import { describe, it, expect, vi } from 'vitest'
import client from '../../../shared/api/client'
import { getProfile, updateProfile, updateAddress } from '../../../features/profile/api/profileApi'
import type { UserProfile, UserAddress } from '../../../features/profile/types'

vi.mock('../../../shared/api/client', () => ({
  default: {
    get: vi.fn(),
    put: vi.fn(),
  },
}))

describe('profileApi', () => {
  const mockProfile: UserProfile = {
    id: '123',
    email: 'deadpool@test.com',
    name: 'Wade Wilson',
    phoneNumber: '+1234567890',
    address: {
      street: '123 Main St',
      city: 'Bengaluru',
      state: 'Karnataka',
      zipcode: '560001',
      country: 'India',
    },
    createdAt: '2023-01-01',
  }

  it('getProfile calls GET /api/me and returns unwrapped data', async () => {
    vi.mocked(client.get).mockResolvedValue({ data: { success: true, data: mockProfile, error: null } })
    const res = await getProfile()
    expect(client.get).toHaveBeenCalledWith('/me')
    expect(res).toEqual(mockProfile)
  })

  it('updateProfile calls PUT /api/me with name and optional phone and returns unwrapped data', async () => {
    vi.mocked(client.put).mockResolvedValue({ data: { success: true, data: mockProfile, error: null } })
    const payload = { fullName: 'Wade Wilson', phoneNumber: '+1234567890' }
    const res = await updateProfile(payload)
    expect(client.put).toHaveBeenCalledWith('/me', payload)
    expect(res).toEqual(mockProfile)
  })

  it('updateAddress calls PUT /api/me/address with address and returns unwrapped data', async () => {
    vi.mocked(client.put).mockResolvedValue({ data: { success: true, data: mockProfile, error: null } })
    const address: UserAddress = mockProfile.address!
    const res = await updateAddress(address)
    expect(client.put).toHaveBeenCalledWith('/me/address', address)
    expect(res).toEqual(mockProfile)
  })
})
