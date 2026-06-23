import { useEffect, useRef, useCallback } from 'react'
import { useWebSocket } from '../context/WebSocketContext'

export interface AvailabilityEvent {
  restaurantId: string
  open: boolean
  restaurantName: string
}

export type MenuEventType =
  | 'ITEM_ADDED'
  | 'ITEM_UPDATED'
  | 'ITEM_AVAILABILITY_CHANGED'
  | 'ITEM_DELETED'

export interface MenuEvent {
  type: MenuEventType
  restaurantId: string
  /** Present for all types except ITEM_DELETED */
  item?: {
    menuItemId: string
    name: string
    description: string | null
    price: number
    category: string
    available: boolean
    imageUrl: string | null
  }
  /** Always present */
  menuItemId: string
}

interface Options {
  restaurantId: string | null
  onAvailability?: (event: AvailabilityEvent) => void
  onMenu?: (event: MenuEvent) => void
}

/**
 * Subscribes to restaurant-scoped availability and menu WebSocket topics.
 * Both callbacks are stable refs — callers don't need to memoize them.
 */
export function useRestaurantSocket({ restaurantId, onAvailability, onMenu }: Options) {
  const { stompClient, isConnected } = useWebSocket()

  // Stable refs so useEffect doesn't re-run when handler identity changes
  const onAvailabilityRef = useRef(onAvailability)
  const onMenuRef = useRef(onMenu)
  useEffect(() => { onAvailabilityRef.current = onAvailability }, [onAvailability])
  useEffect(() => { onMenuRef.current = onMenu }, [onMenu])

  useEffect(() => {
    if (!isConnected || !stompClient || !restaurantId) return

    const subs: ReturnType<typeof stompClient.subscribe>[] = []

    if (onAvailabilityRef.current) {
      const sub = stompClient.subscribe(
        `/topic/restaurant/${restaurantId}/availability`,
        (msg) => {
          try {
            const event: AvailabilityEvent = JSON.parse(msg.body)
            onAvailabilityRef.current?.(event)
          } catch (e) {
            console.error('[useRestaurantSocket] Failed to parse availability event', e)
          }
        }
      )
      subs.push(sub)
    }

    if (onMenuRef.current) {
      const sub = stompClient.subscribe(
        `/topic/restaurant/${restaurantId}/menu`,
        (msg) => {
          try {
            const event: MenuEvent = JSON.parse(msg.body)
            onMenuRef.current?.(event)
          } catch (e) {
            console.error('[useRestaurantSocket] Failed to parse menu event', e)
          }
        }
      )
      subs.push(sub)
    }

    return () => {
      subs.forEach((s) => s.unsubscribe())
    }
  }, [isConnected, stompClient, restaurantId])
}
