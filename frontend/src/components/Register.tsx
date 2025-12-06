import { useState, type FormEventHandler } from "react";
import { fetchApi } from "../util/fetchApi";
import { Loading } from "./Loading";
import type { NotifiableContentContext } from "./NotifiableContainer";

export const Register = ({ setNotification, setError }: NotifiableContentContext) => {
  const passwordNotMatchingError: string = "Error: Passwords don't match";
  const [loading, setLoading] = useState<boolean>(false);
  const [username, setUsername] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [repeatPassword, setRepeatPassword] = useState<string>("");

  const requestRegister: FormEventHandler<HTMLFormElement> = (e) => {
    e.preventDefault();
    if (password != repeatPassword) {
      setError(passwordNotMatchingError);
      return;
    }
    setLoading(true);
    const relPath = "user/register";
    const jsonBody = {
      username: username,
      password: password,
    };
    fetchApi(relPath, "POST", handleResponse, setError, setLoading, jsonBody);
  }

  const handleResponse = async (response: Response) => {
    const message = await response.text();
    console.log(message);
    setNotification("Registrierung erfolgreich! Versuche Login");
  }

  return (
    <div>
      <h2>Registrieren</h2>
      <form onSubmit={requestRegister}>
        <div>
          <label className="form-label">Benutzername:</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div>
          <label className="form-label">Passwort:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <div>
          <label className="form-label">Wiederhole Passwort:</label>
          <input
            type="password"
            value={repeatPassword}
            onChange={(e) => setRepeatPassword(e.target.value)}
            required
          />
        </div>
        {loading && <Loading />}
        <button className="form-submit-button" type="submit">Senden</button>
      </form>
    </div>
  );
}
