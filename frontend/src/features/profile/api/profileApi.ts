import client from '../../../shared/api/client'
import type { ApiResponse } from '../../../shared/api/types'
import type { UserProfile, UserAddress } from '../types'

export const getProfile = async (): Promise<UserProfile> => {
  const { data } = await client.get<ApiResponse<UserProfile>>('/me')
  if (!data.data) {
    throw new Error(data.error?.message || 'Failed to fetch profile')
  }
  return data.data
}

export const updateProfile = async (payload: { fullName: string; phoneNumber?: string }): Promise<UserProfile> => {
  const { data } = await client.put<ApiResponse<UserProfile>>('/me', payload)
  if (!data.data) {
    throw new Error(data.error?.message || 'Failed to update profile')
  }
  return data.data
}

export const updateAddress = async (payload: UserAddress): Promise<UserProfile> => {
  const { data } = await client.put<ApiResponse<UserProfile>>('/me/address', payload)
  if (!data.data) {
    throw new Error(data.error?.message || 'Failed to update address')
  }
  return data.data
}
