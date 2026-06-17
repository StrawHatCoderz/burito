import { Router } from './app/Router'
import { WebSocketProvider } from './shared/context/WebSocketContext'

const App = () => (
  <WebSocketProvider>
    <Router />
  </WebSocketProvider>
)

export default App
