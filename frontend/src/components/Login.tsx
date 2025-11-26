import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchApi } from "../util/fetchApi";
import { Loading } from "./Loading";

export default function Login() {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [username, setUsername] = useState<string>("");
  const [password, setPassword] = useState<string>("");

  const requestLogin = (e: any) => {
    setLoading(true);
    const relPath = "user/login";
    const jsonBody = {
      username: username,
      password: password,
    };
    e.preventDefault();
    fetchApi(relPath, "POST", handleResponse, setError, setLoading, jsonBody);
  }

  const handleResponse = async (response: Response) => {
    const authToken = await response.text();
    localStorage.setItem("authToken", authToken);
    console.log("Login successful: " + authToken);
    navigate("/");
  }

  return (
    <div className="login-container">
      <h2>Login</h2>
      {loading && <Loading />}
      <form onSubmit={requestLogin}>
        <div>
          <label>Benutzername:</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div>
          <label>Passwort:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button type="submit">Login</button>
      </form>
    </div>
  );
}
