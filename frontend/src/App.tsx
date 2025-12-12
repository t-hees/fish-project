import { Route, Routes } from 'react-router-dom'
import FishSearch from './components/FishSearch.tsx'
import LoginPage from './pages/LoginPage.tsx'
import Navbar from './components/Navbar.tsx'
import UserPage from './pages/UserPage.tsx'
import Home from './pages/Home.tsx'
import CreateTrip from './pages/CreateTrip.tsx'

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
                <Route path="/" element={<Home />} />
                <Route path="/fish-search" element={<FishSearch />} />
                <Route path="/user" element={<UserPage />} />
                <Route path="/create-trip" element={<CreateTrip />} />
              </>
            )
          }
          <Route path="/*" element={<LoginPage />} />
        </Routes>
      </div>
    </>
  )
}
