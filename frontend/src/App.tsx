import { Router } from './app/Router'
import { WebSocketProvider } from './shared/context/WebSocketContext'
import { AuthProvider } from './shared/context/AuthContext'

const App = () => (
  <AuthProvider>
    <WebSocketProvider>
      <Router />
    </WebSocketProvider>
  </AuthProvider>
)

export default App

