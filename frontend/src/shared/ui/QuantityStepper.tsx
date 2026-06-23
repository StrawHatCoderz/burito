import IconButton from '@mui/material/IconButton'
import Typography from '@mui/material/Typography'
import CircularProgress from '@mui/material/CircularProgress'

interface QuantityStepperProps {
  quantity: number
  onIncrement: () => void
  onDecrement: () => void
  isLoading?: boolean
  size?: 'small' | 'normal'
  /** Disables only the + (increment) button — decrement remains active */
  incrementDisabled?: boolean
}

export const QuantityStepper = ({
  quantity,
  onIncrement,
  onDecrement,
  isLoading = false,
  size = 'normal',
  incrementDisabled = false,
}: QuantityStepperProps) => {
  const isSmall = size === 'small'

  return (
    <div
      className={`flex items-center justify-between bg-gray-100 rounded-full border border-gray-200 transition-colors ${
        isSmall ? 'h-8 min-w-[70px] px-1' : 'h-10 min-w-[100px] px-1'
      }`}
    >
      <IconButton
        size="small"
        onClick={onDecrement}
        disabled={isLoading}
        aria-label="Decrease quantity"
        sx={{
          color: '#FF5A5F',
          padding: isSmall ? '2px' : '4px',
          '&:hover': { bgcolor: 'rgba(255, 90, 95, 0.1)' }
        }}
      >
        <svg viewBox="0 0 24 24" width={isSmall ? 16 : 20} height={isSmall ? 16 : 20} fill="currentColor">
          <path d="M19 13H5v-2h14v2z" />
        </svg>
      </IconButton>
      
      <div className="flex-1 flex justify-center items-center">
        {isLoading ? (
          <CircularProgress size={isSmall ? 14 : 18} sx={{ color: '#1F2937' }} />
        ) : (
          <Typography
            variant={isSmall ? 'body2' : 'body1'}
            fontWeight={700}
            color="#1F2937"
            sx={{ userSelect: 'none' }}
          >
            {quantity}
          </Typography>
        )}
      </div>

      <IconButton
        size="small"
        onClick={onIncrement}
        disabled={isLoading || incrementDisabled}
        aria-label="Increase quantity"
        sx={{
          color: incrementDisabled ? 'rgba(0,0,0,0.26)' : '#FF5A5F',
          padding: isSmall ? '2px' : '4px',
          '&:hover': { bgcolor: incrementDisabled ? 'transparent' : 'rgba(255, 90, 95, 0.1)' }
        }}
      >
        <svg viewBox="0 0 24 24" width={isSmall ? 16 : 20} height={isSmall ? 16 : 20} fill="currentColor">
          <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z" />
        </svg>
      </IconButton>
    </div>
  )
}
