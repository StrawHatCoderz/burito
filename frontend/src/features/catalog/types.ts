export interface Address {
  addressId: number
  street: string
  city: string
  state: string
  country: string
  zipcode: string
}

export interface Restaurant {
  restaurantId: string
  restaurantName: string
  description: string | null
  cuisineType: string
  rating: number
  estDeliveryMinutes: number
  open: boolean
  imageUrl: string | null
  createdAt: string
  address: Address | null
}

export interface MenuItem {
  menuItemId: string
  name: string
  description: string | null
  price: number
  category: string
  available: boolean
  imageUrl: string | null
}

export interface RestaurantWithMenu {
  restaurant: Restaurant
  menuItems: MenuItem[]
}
