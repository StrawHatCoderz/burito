import { useEffect, useState } from 'react'
import { Box, Typography, Card, CardContent, Button, Stack, Chip, CircularProgress, Divider } from '@mui/material'
import { useAuth } from '../../shared/hooks/useAuth'
import { useWebSocket } from '../../shared/context/WebSocketContext'
import { getActiveOrders, updateOrderStatus } from './adminOrder.api'

export function AdminOrderDashboard() {
  const { restaurantId } = useAuth()
  const { stompClient, isConnected } = useWebSocket()
  const [orders, setOrders] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const data = await getActiveOrders()
        setOrders(data)
      } catch (e) {
        console.error("Failed to fetch orders", e)
      } finally {
        setLoading(false)
      }
    }
    fetchOrders()
  }, [])

  useEffect(() => {
    if (isConnected && stompClient && restaurantId) {
      console.log(`Subscribing to /topic/restaurant/${restaurantId}/orders`)
      const subscription = stompClient.subscribe(`/topic/restaurant/${restaurantId}/orders`, (message) => {
        const newOrder = JSON.parse(message.body)
        console.log('Received new order via WebSocket:', newOrder)
        setOrders(prev => [newOrder, ...prev])
      })

      return () => {
        console.log(`Unsubscribing from /topic/restaurant/${restaurantId}/orders`)
        subscription.unsubscribe()
      }
    }
  }, [isConnected, stompClient, restaurantId])

  const handleAccept = async (orderId: string) => {
    try {
      const updatedOrder = await updateOrderStatus(orderId, 'ACCEPTED')
      setOrders(prev => prev.map(o => o.id === orderId ? updatedOrder : o))
    } catch (e) {
      console.error("Failed to update status", e)
    }
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress />
      </Box>
    )
  }

  const pendingOrders = orders.filter(o => o.status === 'PENDING')
  const acceptedOrders = orders.filter(o => o.status === 'ACCEPTED')

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" fontWeight={800} mb={3} color="#1F2937">Active Orders</Typography>
      
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={4}>
        {/* Pending Column */}
        <Box sx={{ flex: 1 }}>
          <Typography variant="h6" color="#D34A24" mb={2} fontWeight={700}>
            Pending ({pendingOrders.length})
          </Typography>
          <Stack spacing={2}>
            {pendingOrders.map(order => (
              <OrderCard key={order.id} order={order} onAccept={() => handleAccept(order.id)} />
            ))}
            {pendingOrders.length === 0 && (
              <Typography variant="body2" color="text.secondary">No pending orders.</Typography>
            )}
          </Stack>
        </Box>

        {/* Divider for desktop */}
        <Divider orientation="vertical" flexItem sx={{ display: { xs: 'none', md: 'block' } }} />

        {/* Accepted Column */}
        <Box sx={{ flex: 1 }}>
          <Typography variant="h6" color="#10B981" mb={2} fontWeight={700}>
            In Progress ({acceptedOrders.length})
          </Typography>
          <Stack spacing={2}>
            {acceptedOrders.map(order => (
              <OrderCard key={order.id} order={order} />
            ))}
            {acceptedOrders.length === 0 && (
              <Typography variant="body2" color="text.secondary">No orders in progress.</Typography>
            )}
          </Stack>
        </Box>
      </Stack>
    </Box>
  )
}

function OrderCard({ order, onAccept }: { order: any, onAccept?: () => void }) {
  return (
    <Card sx={{ borderRadius: '16px', boxShadow: '0 4px 20px rgba(0,0,0,0.06)', border: '1px solid #f0f0f0' }}>
      <CardContent sx={{ p: 3 }}>
        <Stack direction="row" justifyContent="space-between" mb={1}>
          <Typography variant="subtitle1" fontWeight={800} color="#374151">Order #{order.id.slice(0, 8)}</Typography>
          <Chip 
            label={order.status} 
            sx={{ 
              fontWeight: 700, 
              backgroundColor: order.status === 'PENDING' ? '#FEF3C7' : '#D1FAE5',
              color: order.status === 'PENDING' ? '#D97706' : '#059669'
            }} 
            size="small" 
          />
        </Stack>
        <Typography variant="body2" color="text.secondary" mb={2} fontWeight={500}>Customer: {order.customerName}</Typography>
        
        <Divider sx={{ my: 1.5, borderStyle: 'dashed' }} />
        
        <Stack spacing={1.5} my={2}>
          {order.items.map((item: any) => (
            <Stack direction="row" justifyContent="space-between" key={item.id}>
              <Typography variant="body2" fontWeight={600} color="#4B5563">{item.quantity}x {item.name}</Typography>
              <Typography variant="body2" fontWeight={600} color="#4B5563">₹{item.priceAtCheckout * item.quantity}</Typography>
            </Stack>
          ))}
        </Stack>

        <Divider sx={{ my: 1.5, borderStyle: 'dashed' }} />
        
        <Stack direction="row" justifyContent="space-between" alignItems="center" mt={2}>
          <Typography variant="subtitle1" fontWeight={800} color="#111827">Total: ₹{order.totalAmount}</Typography>
          {onAccept && (
            <Button 
              variant="contained" 
              onClick={onAccept} 
              sx={{ 
                borderRadius: '8px', 
                fontWeight: 700,
                textTransform: 'none',
                backgroundColor: '#D34A24',
                '&:hover': { backgroundColor: '#B23A1A' }
              }}
            >
              Accept Order
            </Button>
          )}
        </Stack>
      </CardContent>
    </Card>
  )
}
