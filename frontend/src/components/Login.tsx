import { useState, type FormEventHandler } from "react";
import { useNavigate } from "react-router-dom";
import { fetchApi } from "../util/fetchApi";
import { Loading } from "./Loading";

export default function Login() {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [username, setUsername] = useState<string>("");
  const [password, setPassword] = useState<string>("");

  const requestLogin: FormEventHandler<HTMLFormElement> = (e) => {
    e.preventDefault();
    setLoading(true);
    const relPath = "user/login";
    const jsonBody = {
      username: username,
      password: password,
    };
    fetchApi(relPath, "POST", handleResponse, setError, setLoading, jsonBody);
  }

  const handleResponse = async (response: Response) => {
    const authToken = await response.text();
    localStorage.setItem("auth-token", authToken);
    localStorage.setItem("username", username);
    console.log("Login successful: " + authToken);
    navigate("/");
  }

  return (
    <div>
      <h2>Login</h2>
      <form onSubmit={requestLogin}>
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
        {error && <p style={{ color: 'red' }}>{error}</p>}
        {loading && <Loading />}
        <button className="form-submit-button" type="submit">Senden</button>
      </form>
    </div>
  );
}
