import { useNavigate } from "react-router-dom";
import "./Navbar.css";
import { fetchApi } from "../util/fetchApi";
import { useAuth } from "../util/AuthContext";
import { languages, useTranslation } from "../i18n/LanguageContext";

export default function Navbar() {
  const navigate = useNavigate();
  const {name, authClear} = useAuth();
  const {language, setLanguage, t} = useTranslation();
  const nextLanguage = languages[(languages.indexOf(language) + 1) % languages.length];

  const userLogout = () => {
    fetchApi("auth/logout", "POST",
      () => {authClear(); navigate("/login")},
      (error) => console.error(error),
      () => {});
  }

  return (
    <nav className="navigation">
      <button type="button" onClick={() => navigate("/")}>
        {t.navbar.home}
      </button>
      {name
        ? <>
            <button type="button" onClick={() => navigate("/user")}>
              {name}
            </button>
            <button type="button" onClick={userLogout}>
              {t.navbar.logout}
            </button>
          </>
        : <>
            <button type="button" onClick={() => navigate("/login")}>
              {t.navbar.login}
            </button>
          </>
      }
      <button type="button" title={t.navbar.switchLanguage} onClick={() => setLanguage(nextLanguage)}>
        {nextLanguage.toUpperCase()}
      </button>
    </nav>
  )
}
