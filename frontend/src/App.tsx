import { Route, Routes } from 'react-router-dom'
import FishSearch from './components/FishSearch.tsx'
import Login from './components/Login.tsx'

export default function App() {

  return (
      <Routes>
        <Route path="/fish-search" element={<FishSearch />} />
        <Route path="/login" element={<Login />} />
      </Routes>
  )
}
