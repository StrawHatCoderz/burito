import Typography from '@mui/material/Typography'

const PROMOTIONS = [
  {
    id: 1,
    badge: 'Limited Time',
    title: 'Free Delivery',
    subtitle: 'On your first order over ₹400',
    gradient: 'from-[#FF5A5F] to-[#E03C31]',
  },
  {
    id: 2,
    badge: 'New',
    title: '20% Off Sushi',
    subtitle: 'Fresh rolls at Sushi Station',
    gradient: 'from-emerald-400 to-teal-600',
  },
  {
    id: 3,
    badge: 'Trending',
    title: 'Burger Bonanza',
    subtitle: 'Buy 1 get 1 free at Smash Bros',
    gradient: 'from-amber-400 to-orange-500',
  },
]

export const PromoCarousel = () => {
  return (
    <div className="mb-8">
      <div className="flex gap-4 overflow-x-auto pb-6 snap-x snap-mandatory hide-scrollbar px-1">
        {PROMOTIONS.map((promo) => (
          <div
            key={promo.id}
            className={`flex-none w-[85%] md:w-[400px] h-32 md:h-36 rounded-[28px] bg-gradient-to-r ${promo.gradient} p-5 md:p-6 text-white snap-center sm:snap-start relative overflow-hidden shadow-md hover:shadow-xl transition-all duration-300 hover:-translate-y-1 cursor-pointer group flex items-center border border-white/10`}
          >
            {/* Dynamic right-side graphics to break the "standard card" look */}
            <div className="absolute right-0 top-0 bottom-0 w-1/2 overflow-hidden pointer-events-none">
              <div className="absolute right-[-15%] top-[-30%] w-40 h-40 rounded-full bg-white opacity-[0.15] mix-blend-overlay group-hover:scale-125 transition-transform duration-700 ease-out"></div>
              <div className="absolute right-[10%] bottom-[-20%] w-24 h-24 rounded-full bg-black opacity-10 group-hover:scale-110 transition-transform duration-500 delay-75 ease-out"></div>
            </div>
            
            {/* Content left-aligned */}
            <div className="relative z-10 flex flex-col h-full justify-center w-[65%] pr-2">
              <div className="mb-2">
                <span className="inline-block bg-black/20 backdrop-blur-md px-2.5 py-1 rounded-lg text-[10px] md:text-xs font-black uppercase tracking-widest text-white/90">
                  {promo.badge}
                </span>
              </div>
              
              <Typography variant="h6" component="h3" fontWeight={900} sx={{ letterSpacing: '-0.03em', lineHeight: 1.1, mb: 0.5, fontSize: { xs: '1.25rem', md: '1.5rem' } }}>
                {promo.title}
              </Typography>
              <Typography variant="body2" className="opacity-90 font-medium text-sm line-clamp-1">
                {promo.subtitle}
              </Typography>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
