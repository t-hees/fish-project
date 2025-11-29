import { Route, Routes } from 'react-router-dom'
import FishSearch from './components/FishSearch.tsx'
import LoginPage from './pages/LoginPage.tsx'
import Navbar from './components/Navbar.tsx'

export default function App() {

  return (
    <div>
      <Navbar />
      <Routes>
        <Route path="/fish-search" element={<FishSearch />} />
        <Route path="/login" element={<LoginPage />} />
      </Routes>
    </div>
  )
}
