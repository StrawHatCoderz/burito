import { useEffect, useState } from 'react'
import { Container, Typography, Stepper, Step, StepLabel, Box, CircularProgress, Paper } from '@mui/material'
import { getActiveOrder } from './api/orders.api'
import { useNavigate } from 'react-router-dom'
import { AxiosError } from 'axios'
import { useWebSocket } from '../../shared/context/WebSocketContext'

const steps = ['Pending', 'Accepted', 'Delivered']

export const ActiveOrderPage = () => {
  const [loading, setLoading] = useState(true)
  const [order, setOrder] = useState<any>(null)
  const navigate = useNavigate()
  const { stompClient, isConnected } = useWebSocket()

  useEffect(() => {
    const fetchOrder = async () => {
      try {
        const data = await getActiveOrder()
        setOrder(data)
      } catch (err) {
        const axiosError = err as AxiosError
        if (axiosError.response?.status === 404) {
          navigate('/restaurants')
        }
      } finally {
        setLoading(false)
      }
    }
    fetchOrder()
  }, [navigate])

  useEffect(() => {
    if (!isConnected || !stompClient || !order?.id) return

    const sub = stompClient.subscribe('/user/queue/orders', (msg) => {
      try {
        const event = JSON.parse(msg.body)
        if (event.orderId === order.id) {
          setOrder((prev: any) => ({ ...prev, status: event.status }))
        }
      } catch (e) {
        console.error('Failed to parse order event', e)
      }
    })

    return () => sub.unsubscribe()
  }, [isConnected, stompClient, order?.id])

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}>
        <CircularProgress />
      </Box>
    )
  }

  if (!order) {
    return null
  }

  const getActiveStep = () => {
    switch (order.status) {
      case 'PENDING': return 0
      case 'ACCEPTED': return 1
      case 'DELIVERED': return 3
      default: return 0
    }
  }

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Paper sx={{ p: 4 }}>
        <Typography variant="h4" gutterBottom>
          Active Order Tracking
        </Typography>
        <Typography variant="subtitle1" color="text.secondary" gutterBottom>
          Order ID: {order.id}
        </Typography>
        <Typography variant="subtitle1" color="text.secondary" gutterBottom>
          Total: ${order.totalAmount?.toFixed(2)}
        </Typography>

        <Box sx={{ width: '100%', mt: 6, mb: 4 }}>
          <Stepper activeStep={getActiveStep()} alternativeLabel>
            {steps.map((label) => (
              <Step key={label}>
                <StepLabel>{label}</StepLabel>
              </Step>
            ))}
          </Stepper>
        </Box>
      </Paper>
    </Container>
  )
}
