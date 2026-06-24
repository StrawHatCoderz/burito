export interface UserAddress {
  street: string
  city: string
  state: string
  zipcode: string
  country: string
}

export interface UserProfile {
  id: string
  email: string
  name: string
  phoneNumber?: string
  address?: UserAddress | null
  createdAt: string
}
