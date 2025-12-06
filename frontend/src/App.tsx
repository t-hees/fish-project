import { Route, Routes } from 'react-router-dom'
import FishSearch from './components/FishSearch.tsx'
import LoginPage from './pages/LoginPage.tsx'
import Navbar from './components/Navbar.tsx'
import UserPage from './pages/UserPage.tsx'

export default function App() {

  return (
    <>
      <Navbar />
      <div id="main-content-container">
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          {localStorage.getItem("auth-token")
            && (
              <>
                <Route path="/fish-search" element={<FishSearch />} />
                <Route path="/user" element={<UserPage />} />
              </>
            )
          }
          <Route path="/*" element={<LoginPage />} />
        </Routes>
      </div>
    </>
  )
}
