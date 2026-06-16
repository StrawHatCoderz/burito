interface MenuCategoryNavProps {
  categories: string[]
  activeCategory: string
  onSelect: (category: string) => void
}

const formatLabel = (category: string) =>
  category.charAt(0) + category.slice(1).toLowerCase()

export const MenuCategoryNav = ({ categories, activeCategory, onSelect }: MenuCategoryNavProps) => {
  if (categories.length === 0) return null

  return (
    <nav className="sticky top-0 z-10 bg-bg-surface border-b border-border shadow-sm w-full overflow-x-auto hide-scrollbar">
      <div className="flex px-4 md:px-8">
        {categories.map((category) => {
          const isActive = activeCategory === category
          return (
            <button
              key={category}
              onClick={() => onSelect(category)}
              className={`whitespace-nowrap px-4 py-3 text-sm font-body font-semibold transition-colors duration-200 border-b-2 ${
                isActive
                  ? 'border-accent text-accent'
                  : 'border-transparent text-text-muted hover:text-text-primary'
              }`}
            >
              {formatLabel(category)}
            </button>
          )
        })}
      </div>
    </nav>
  )
}
