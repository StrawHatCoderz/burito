import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { LoginPage } from '../features/auth/LoginPage'
import { RegisterPage } from '../features/auth/RegisterPage'
import { ProtectedRoute } from './ProtectedRoute'
import { HomePage } from '../features/home/HomePage'
import { RestaurantsPage } from '../features/catalog/RestaurantsPage'
import { RestaurantDetailPage } from '../features/catalog/RestaurantDetailPage'
import { NavBar } from '../shared/ui/NavBar'
import { CartDrawer } from '../features/cart/CartDrawer'
import { AdminLoginPage } from '../features/admin/AdminLoginPage'
import { AdminRegisterPage } from '../features/admin/AdminRegisterPage'

export const Router = () => (
  <BrowserRouter>
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/admin/login" element={<AdminLoginPage />} />
      <Route path="/admin/register" element={<AdminRegisterPage />} />
      <Route element={<NavBar />}>
        <Route path="/restaurants" element={<RestaurantsPage />} />
        <Route path="/restaurants/:id" element={<RestaurantDetailPage />} />
        <Route path="/" element={<Navigate to="/restaurants" replace />} />
        <Route path="*" element={<Navigate to="/restaurants" replace />} />
      </Route>
    </Routes>
    <CartDrawer />
  </BrowserRouter>
)
