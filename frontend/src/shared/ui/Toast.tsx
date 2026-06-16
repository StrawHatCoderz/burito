import Snackbar from '@mui/material/Snackbar'
import Alert from '@mui/material/Alert'
import Slide from '@mui/material/Slide'
import type { SlideProps } from '@mui/material/Slide'

interface ToastProps {
  open: boolean
  message: string
  onClose: () => void
  severity?: 'error' | 'success' | 'info' | 'warning'
}

function SlideTransition(props: SlideProps) {
  return <Slide {...props} direction="up" />
}

export const Toast = ({ open, message, onClose, severity = 'error' }: ToastProps) => {
  return (
    <Snackbar
      open={open}
      autoHideDuration={4000}
      onClose={onClose}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      TransitionComponent={SlideTransition}
    >
      <Alert
        onClose={onClose}
        severity={severity}
        sx={{
          backgroundColor: 'rgba(17, 24, 39, 0.9)',
          backdropFilter: 'blur(8px)',
          color: '#fff',
          borderRadius: '9999px',
          fontWeight: 500,
          boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
          px: 3,
          alignItems: 'center',
          '& .MuiAlert-icon': {
            color: severity === 'error' ? '#FF5A5F' : '#10B981',
          }
        }}
      >
        {message}
      </Alert>
    </Snackbar>
  )
}
