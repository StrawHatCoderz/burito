import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { fetchRestaurantWithMenu } from './catalogApi'
import { MenuSection } from './MenuSection'
import { MenuCategoryNav } from './MenuCategoryNav'
import type { RestaurantWithMenu } from './types'

const CATEGORY_ORDER = ['STARTERS', 'MAINS', 'SIDES', 'DESSERTS', 'BEVERAGES']

type Status = 'loading' | 'success' | 'not-found' | 'error'

const is404 = (error: unknown) =>
  (error as { response?: { status?: number } }).response?.status === 404

export const RestaurantDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [status, setStatus] = useState<Status>('loading')
  const [data, setData] = useState<RestaurantWithMenu | null>(null)
  const [activeCategory, setActiveCategory] = useState<string>('')

  useEffect(() => {
    if (!id) return
    fetchRestaurantWithMenu(id)
      .then((result) => {
        setData(result)
        setStatus('success')
        
        // Setup initial active category
        const availableCats = CATEGORY_ORDER.filter(
          cat => result.menuItems.some(i => i.category === cat)
        )
        if (availableCats.length > 0) {
          setActiveCategory(availableCats[0])
        }
      })
      .catch((error) => setStatus(is404(error) ? 'not-found' : 'error'))
  }, [id])

  // Simple scroll handler
  const handleCategorySelect = (category: string) => {
    setActiveCategory(category)
    const element = document.getElementById(`category-${category.toLowerCase()}`)
    if (element) {
      const yOffset = -80 // Account for sticky nav
      const y = element.getBoundingClientRect().top + window.pageYOffset + yOffset
      window.scrollTo({ top: y, behavior: 'smooth' })
    }
  }

  if (status === 'loading') {
    return (
      <div className="min-h-screen thermal-sizzle flex flex-col items-center justify-center p-4">
        <div className="bg-bg-surface/80 backdrop-blur-sm p-6 rounded-2xl md:rounded-lg shadow-sm border border-white/20">
          <h2 className="text-xl font-display font-semibold text-text-primary animate-pulse">
            Warming up the kitchen...
          </h2>
        </div>
      </div>
    )
  }

  if (status === 'not-found') {
    return (
      <div className="min-h-screen bg-bg-primary flex flex-col items-center justify-center gap-4 p-4">
        <h2 className="text-3xl font-display font-bold text-text-primary">
          Restaurant not found
        </h2>
        <p className="text-text-muted font-body text-center max-w-md">We couldn't find the menu you're looking for. It might have been removed or the URL is incorrect.</p>
        <button 
          onClick={() => navigate('/restaurants')}
          className="mt-4 px-6 py-3 bg-bg-surface border border-border rounded-md font-body font-semibold text-text-primary hover:bg-accent-subtle hover:text-accent transition-colors shadow-sm"
        >
          Back to restaurants
        </button>
      </div>
    )
  }

  if (status === 'error') {
    return (
      <div className="min-h-screen bg-bg-primary flex flex-col items-center justify-center gap-4 p-4">
        <div className="bg-[#FFF0F0] border border-[#FFCDCD] text-accent px-6 py-5 rounded-xl md:rounded-lg shadow-sm text-center max-w-md">
          <h3 className="font-display font-bold text-xl mb-2">Unable to load menu</h3>
          <p className="font-body text-sm opacity-90 leading-relaxed">Please check your internet connection and try again.</p>
        </div>
        <button 
          onClick={() => navigate('/restaurants')}
          className="mt-2 px-6 py-3 bg-bg-surface border border-border rounded-md font-body font-semibold text-text-primary hover:bg-accent-subtle hover:text-accent transition-colors shadow-sm"
        >
          Back to restaurants
        </button>
      </div>
    )
  }

  const { restaurant, menuItems } = data!

  const menuSections = CATEGORY_ORDER.map((cat) => ({
    category: cat,
    items: menuItems.filter((i) => i.category === cat),
  })).filter((s) => s.items.length > 0)
  
  const availableCategories = menuSections.map(s => s.category)

  const DEFAULT_RESTAURANT_IMAGE = 'https://placehold.co/1200x400/eeeeee/999999?text=No+Image+Available'
  const bannerImg = restaurant.imageUrl || DEFAULT_RESTAURANT_IMAGE

  return (
    <div className="min-h-screen bg-bg-primary pb-24">
      {/* Banner Image */}
      <div className="w-full h-48 md:h-64 bg-gray-200">
        <img src={bannerImg} alt={restaurant.restaurantName} className="w-full h-full object-cover" />
      </div>

      {/* Compact Header */}
      <div className="bg-bg-surface px-4 py-4 md:px-8 border-b border-border shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-3">
        <div className="flex items-center gap-3 md:gap-4">
          <button 
            onClick={() => navigate('/restaurants')} 
            className="w-8 h-8 -ml-2 text-text-muted hover:text-text-primary hover:bg-bg-primary rounded-full transition-colors flex items-center justify-center shrink-0"
            aria-label="Back to Discovery"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="19" y1="12" x2="5" y2="12"></line><polyline points="12 19 5 12 12 5"></polyline></svg>
          </button>
          
          <div>
            <h1 className="text-lg md:text-xl font-display font-bold text-text-primary leading-tight">
              {restaurant.restaurantName}
            </h1>
            <div className="flex items-center flex-wrap gap-1.5 md:gap-2 text-xs font-body text-text-muted mt-0.5 font-medium">
              <span className="uppercase tracking-wider font-semibold">{restaurant.cuisineType.replace(/_/g, ' ')}</span>
              <span className="text-border text-[10px]">●</span>
              <span className="flex items-center gap-0.5 text-text-primary">
                <span className="text-accent">★</span> {restaurant.rating.toFixed(1)}
              </span>
              <span className="text-border text-[10px]">●</span>
              <span className="flex items-center gap-1">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="opacity-70"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                {restaurant.estDeliveryMinutes} min
              </span>
            </div>
          </div>
        </div>
        
        <div className="shrink-0 flex items-center">
          <span className={`inline-flex items-center px-2.5 py-1 rounded-md text-[10px] font-body font-bold tracking-widest uppercase border ${
            restaurant.open 
              ? 'bg-[#F0FDF4] text-[#166534] border-[#DCFCE7]' 
              : 'bg-bg-primary text-text-muted border-border'
          }`}>
            {restaurant.open ? '• Open' : 'Closed'}
          </span>
        </div>
      </div>

      {/* Sticky Navigation */}
      <MenuCategoryNav 
        categories={availableCategories} 
        activeCategory={activeCategory} 
        onSelect={handleCategorySelect} 
      />

      {/* Menu Content */}
      <div className="px-4 py-8 md:px-8 max-w-3xl mx-auto">
        {!restaurant.open && (
          <div className="mb-8 bg-bg-surface border border-border rounded-xl md:rounded-lg p-5 text-center shadow-sm">
            <h3 className="font-display font-semibold text-text-primary text-lg">Restaurant is currently closed</h3>
            <p className="text-sm text-text-muted font-body mt-1">You can browse the menu, but ordering is disabled.</p>
          </div>
        )}

        {menuSections.length === 0 ? (
          <div className="text-center py-16 bg-bg-surface rounded-2xl md:rounded-lg border border-border border-dashed">
            <p className="text-text-muted font-body text-lg">Menu is currently unavailable.</p>
          </div>
        ) : (
          menuSections.map((section) => (
            <MenuSection key={section.category} category={section.category} items={section.items} />
          ))
        )}
      </div>
    </div>
  )
}
