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
      <button type="button" onClick={() => navigate("/")}>
        HOME
      </button>
      {localStorage.getItem("auth-token")
        ? <>
            <button type="button" onClick={() => navigate("/user")}>
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
