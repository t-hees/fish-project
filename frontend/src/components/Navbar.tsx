import { useNavigate } from "react-router-dom";
import "./Navbar.css";
import { fetchApi } from "../util/fetchApi";
import { useContext } from "react";
import { AuthContext } from "../util/AuthContext";

export default function Navbar() {
  const navigate = useNavigate();
  const {name, authClear} = useContext(AuthContext);

  const userLogout = () => {
    fetchApi("user/logout", "POST",
      () => {console.log("successfully logged out user"); authClear(); navigate("/login")},
      (error) => console.error(error),
      () => console.log("requesting user logout"));
  }

  return (
    <nav className="navigation">
      <button type="button" onClick={() => navigate("/")}>
        HOME
      </button>
      {name
        ? <>
            <button type="button" onClick={() => navigate("/user")}>
              {name}
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
