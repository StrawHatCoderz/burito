// Re-export from AuthContext so all consumers share a single auth state.
// The old standalone hook had independent useState per component,
// which meant login() in LoginPage never triggered a re-render in CartDrawer.
export { useAuthContext as useAuth } from '../context/AuthContext'
