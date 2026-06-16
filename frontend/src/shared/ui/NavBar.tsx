import { Link, Outlet } from 'react-router-dom'
import AppBar from '@mui/material/AppBar'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import IconButton from '@mui/material/IconButton'
import Badge from '@mui/material/Badge'
import { CartIcon } from './icons/CartIcon'
import { useCart } from '../../features/cart/CartContext'

export const NavBar = () => {
  const { cart, openCartDrawer } = useCart()

  return (
    <>
      <AppBar position="sticky" sx={{ bgcolor: '#ffffff', color: '#1F2937', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
        <Toolbar sx={{ justifyContent: 'space-between', paddingX: { xs: 2, sm: 4 } }}>
          <Typography
            variant="h5"
            component={Link}
            to="/restaurants"
            sx={{ textDecoration: 'none', color: '#FF5A5F', fontWeight: 800, letterSpacing: '-0.5px' }}
          >
            Burito
          </Typography>
          
          <IconButton onClick={openCartDrawer} aria-label="Open cart" sx={{ transition: 'transform 0.2s', '&:hover': { transform: 'scale(1.05)' }}}>
            <Badge
              badgeContent={cart.cartItemCount}
              sx={{
                '& .MuiBadge-badge': {
                  bgcolor: '#FF5A5F',
                  color: 'white',
                  fontWeight: 'bold',
                }
              }}
            >
              <CartIcon />
            </Badge>
          </IconButton>
        </Toolbar>
      </AppBar>
      <Outlet />
    </>
  )
}
