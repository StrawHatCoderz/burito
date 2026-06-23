import { useEffect, useState } from 'react'
import { useWebSocket } from '../context/WebSocketContext'
import { useAuth } from '../hooks/useAuth'
import { Toast } from './Toast'

interface OrderStatusEvent {
  orderId: string
  customerId: string
  status: 'PENDING' | 'ACCEPTED' | 'DELIVERED' | 'CANCELLED'
  restaurantId: string
}

const STATUS_CONFIG: Record<
  OrderStatusEvent['status'],
  { message: string; severity: 'success' | 'info' | 'error' | 'warning' } | null
> = {
  PENDING: null, // customer triggered this themselves — no toast needed
  ACCEPTED: {
    message: '🎉 Your order has been accepted! It\'s being prepared now.',
    severity: 'success',
  },
  DELIVERED: {
    message: '✅ Your order has been delivered. Enjoy your meal!',
    severity: 'success',
  },
  CANCELLED: {
    message: '❌ Your order has been cancelled by the restaurant.',
    severity: 'error',
  },
}

/**
 * Mount once in the app tree (Router.tsx).
 * Subscribes to /user/queue/orders and shows a toast on every status transition
 * relevant to the logged-in customer.
 */
export const OrderNotificationListener = () => {
  const { isAuthenticated } = useAuth()
  const { stompClient, isConnected } = useWebSocket()
  const [toast, setToast] = useState<{ open: boolean; message: string; severity: 'success' | 'info' | 'error' | 'warning' }>({
    open: false,
    message: '',
    severity: 'info',
  })

  useEffect(() => {
    if (!isAuthenticated || !isConnected || !stompClient) return

    const sub = stompClient.subscribe('/user/queue/orders', (msg) => {
      try {
        const event: OrderStatusEvent = JSON.parse(msg.body)
        const config = STATUS_CONFIG[event.status]
        if (config) {
          setToast({ open: true, message: config.message, severity: config.severity })
        }
      } catch (e) {
        console.error('[OrderNotificationListener] Failed to parse order event', e)
      }
    })

    return () => sub.unsubscribe()
  }, [isAuthenticated, isConnected, stompClient])

  if (!isAuthenticated) return null

  return (
    <Toast
      open={toast.open}
      message={toast.message}
      severity={toast.severity}
      onClose={() => setToast((prev) => ({ ...prev, open: false }))}
    />
  )
}
