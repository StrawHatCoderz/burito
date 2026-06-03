import Chip from '@mui/material/Chip'
import Typography from '@mui/material/Typography'
import type { MenuItem } from './types'

interface MenuSectionProps {
  category: string
  items: MenuItem[]
}

const formatLabel = (category: string) =>
  category.charAt(0) + category.slice(1).toLowerCase()

export const MenuSection = ({ category, items }: MenuSectionProps) => (
  <div className="mb-6">
    <Typography variant="h6" component="h3" fontWeight={600} gutterBottom>
      {formatLabel(category)}
    </Typography>
    <div className="flex flex-col gap-3">
      {items.map((item) => (
        <div
          key={item.menuItemId}
          className={`flex items-start justify-between gap-4 p-3 bg-white rounded-lg border ${!item.available ? 'opacity-50' : ''}`}
        >
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <Typography variant="body1" fontWeight={500}>
                {item.name}
              </Typography>
              {!item.available && (
                <Chip label="Unavailable" size="small" variant="outlined" />
              )}
            </div>
            {item.description && (
              <Typography variant="body2" color="text.secondary" className="mt-0.5">
                {item.description}
              </Typography>
            )}
          </div>
          <Typography variant="body1" fontWeight={500} className="shrink-0">
            ₹{item.price.toFixed(2)}
          </Typography>
        </div>
      ))}
    </div>
  </div>
)
