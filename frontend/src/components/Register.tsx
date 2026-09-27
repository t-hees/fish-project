import { useState, type ChangeEventHandler } from "react";
import { useTranslation } from "../i18n/LanguageContext";
import { fetchApi } from "../util/fetchApi";
import { Loading } from "./Loading";
import type { NotifiableContentContext } from "./NotifiableContainer";

export const Register = ({ setNotification, setError }: NotifiableContentContext) => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState<boolean>(false);
  const [username, setUsername] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [repeatPassword, setRepeatPassword] = useState<string>("");

  const requestRegister: ChangeEventHandler<HTMLFormElement> = (e) => {
    e.preventDefault();
    if (password != repeatPassword) {
      setError(t.loginPage.passwordsDontMatch);
      return;
    }
    setLoading(true);
    const relPath = "auth/register";
    const jsonBody = {
      username: username,
      password: password,
    };
    fetchApi(relPath, "POST", handleResponse, setError, setLoading, jsonBody);
  }

  const handleResponse = async () => {
    setNotification(t.loginPage.registerSuccess);
  }

  return (
    <div>
      <h2>{t.loginPage.registerTitle}</h2>
      <form onSubmit={requestRegister}>
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
            minLength={8}
            maxLength={72}
            required
          />
        </div>
        <div>
          <label className="form-label">{t.loginPage.repeatPassword}</label>
          <input
            type="password"
            value={repeatPassword}
            onChange={(e) => setRepeatPassword(e.target.value)}
            required
          />
        </div>
        {loading && <Loading />}
        <button className="form-submit-button" type="submit">{t.common.send}</button>
      </form>
    </div>
  );
}
