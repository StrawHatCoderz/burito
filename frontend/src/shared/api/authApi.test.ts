import { describe, it, expect, vi } from 'vitest'
import * as authApi from './authApi'
import client from './client'

vi.mock('./client')

describe('authApi', () => {
  it('login should post to /auth/login', async () => {
    const credentials = { email: 'test@test.com', password: 'password' }
    vi.mocked(client.post).mockResolvedValue({ data: { success: true } })

    const result = await authApi.login(credentials)

    expect(client.post).toHaveBeenCalledWith('/auth/login', credentials)
    expect(result).toEqual({ success: true })
  })

  it('adminLogin should post to /admin/auth/login', async () => {
    const credentials = { email: 'admin@test.com', password: 'password' }
    vi.mocked(client.post).mockResolvedValue({ data: { success: true } })

    const result = await authApi.adminLogin(credentials)

    expect(client.post).toHaveBeenCalledWith('/admin/auth/login', credentials)
    expect(result).toEqual({ success: true })
  })

  it('adminRegister should post to /admin/auth/register', async () => {
    const payload = { full_name: 'Admin', email: 'admin@test.com', password: 'password' }
    vi.mocked(client.post).mockResolvedValue({ data: { success: true } })

    const result = await authApi.adminRegister(payload)

    expect(client.post).toHaveBeenCalledWith('/admin/auth/register', payload)
    expect(result).toEqual({ success: true })
  })

  it('register should post to /auth/register', async () => {
    const payload = { full_name: 'Customer', email: 'customer@test.com', password: 'password' }
    vi.mocked(client.post).mockResolvedValue({ data: { success: true } })

    const result = await authApi.register(payload)

    expect(client.post).toHaveBeenCalledWith('/auth/register', payload)
    expect(result).toEqual({ success: true })
  })
})
