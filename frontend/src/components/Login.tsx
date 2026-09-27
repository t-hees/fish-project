import { useState, type FormEventHandler } from "react";
import { useNavigate } from "react-router-dom";
import { fetchApi } from "../util/fetchApi";
import { Loading } from "./Loading";
import type { NotifiableContentContext } from "./NotifiableContainer";
import { useAuth, type UserInfo } from "../util/AuthContext";
import { useTranslation } from "../i18n/LanguageContext";

export const Login = ({ setError }: NotifiableContentContext) => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState<boolean>(false);
  const [username, setUsername] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const authContext = useAuth();
  const { t } = useTranslation();

  const requestLogin: FormEventHandler<HTMLFormElement> = (e) => {
    e.preventDefault();
    setLoading(true);
    const relPath = "auth/login";
    const jsonBody = {
      username: username,
      password: password,
    };
    fetchApi(relPath, "POST", handleResponse, setError, setLoading, jsonBody);
  }

  const handleResponse = async (response: Response) => {
    const user: UserInfo = await response.json();
    authContext.setName(user.username);
    navigate("/");
  }

  return (
    <div>
      <h2>{t.loginPage.loginTitle}</h2>
      <form onSubmit={requestLogin}>
        <div>
          <label className="form-label">{t.common.username}</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div>
          <label className="form-label">{t.common.password}</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        {loading && <Loading />}
        <button className="form-submit-button" type="submit">{t.common.send}</button>
      </form>
    </div>
  );
}
