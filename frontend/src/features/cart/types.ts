export interface CartItemLocal {
  cartItemId: string
  menuItemId: string
  name: string
  quantity: number
  unitPrice: number
  subtotal: number
}

export interface CartView {
  cartId: string | null
  restaurantId: string | null
  items: CartItemLocal[]
  total: number
}

export interface CartState extends CartView {
  cartItemCount: number
}
