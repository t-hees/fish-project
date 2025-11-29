import { useState } from "react"
import { useNavigate } from "react-router-dom";
import "./Navbar.css";

export default function Navbar() {
  const navigate = useNavigate();

  const userLogout = () => {
    localStorage.clear();
    navigate("/login");
  }

  return (
    <nav className="navigation">
      <div className="nav-home-div">
        <a href="/">HOME</a>
      </div>
      {localStorage.getItem("auth-token")
        ? <>
            <button>
              {localStorage.getItem("username")}
            </button>
            <button type="button" onClick={userLogout}>
              LOGOUT
            </button>
          </>
        : <>
            <button type="button" onClick={() => navigate("/login")}>
              LOGIN
            </button>
          </>
      }
    </nav>
  )
}
