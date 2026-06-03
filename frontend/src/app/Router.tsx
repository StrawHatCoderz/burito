import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { LoginPage } from '../features/auth/LoginPage'
import { RegisterPage } from '../features/auth/RegisterPage'
import { ProtectedRoute } from './ProtectedRoute'
import { HomePage } from '../features/home/HomePage'
import { RestaurantsPage } from '../features/catalog/RestaurantsPage'
import { RestaurantDetailPage } from '../features/catalog/RestaurantDetailPage'

export const Router = () => (
  <BrowserRouter>
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/restaurants" element={<RestaurantsPage />} />
      <Route path="/restaurants/:id" element={<RestaurantDetailPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <HomePage />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/restaurants" replace />} />
    </Routes>
  </BrowserRouter>
)
