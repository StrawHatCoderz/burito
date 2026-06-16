import { useState } from 'react'
import { Toast } from '../../shared/ui/Toast'
import { MenuItemCard } from './MenuItemCard'
import type { MenuItem } from './types'

interface MenuSectionProps {
  category: string
  items: MenuItem[]
}

const formatLabel = (category: string) =>
  category.charAt(0) + category.slice(1).toLowerCase()

export const MenuSection = ({ category, items }: MenuSectionProps) => {
  const [errorToast, setErrorToast] = useState<{ open: boolean; message: string }>({ open: false, message: '' })

  const handleError = (message: string) => setErrorToast({ open: true, message })
  const closeToast = () => setErrorToast((prev) => ({ ...prev, open: false }))

  // Note: we assign an ID for the sticky navigation to target
  const sectionId = `category-${category.toLowerCase()}`

  return (
    <div id={sectionId} className="mb-8 scroll-mt-24">
      <h3 className="text-xl font-display font-semibold text-text-primary mb-4 pb-2 border-b border-border">
        {formatLabel(category)}
      </h3>
      <div className="flex flex-col gap-3">
        {[...items]
          .sort((a, b) => {
            if (a.available === b.available) return 0;
            return a.available ? -1 : 1;
          })
          .map((item) => (
            <MenuItemCard key={item.menuItemId} item={item} onError={handleError} />
          ))}
      </div>
      <Toast open={errorToast.open} message={errorToast.message} onClose={closeToast} />
    </div>
  )
}
