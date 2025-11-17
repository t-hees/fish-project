import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import FishSearch from './components/FishSearch.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <FishSearch />
  </StrictMode>,
)
