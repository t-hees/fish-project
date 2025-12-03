import { useState } from "react";
import Login from "../components/Login.tsx";
import Register from "../components/Register.tsx";

export default function LoginPage() {
  const [noAccount, setNoAccount] = useState<boolean>(false);
  const [notification, setNotification] = useState<string>("");

  const handleRegisterSuccess = () => {
    setNotification("Registrierung erfolgreich! Versuche Login");
    setNoAccount(false);
  }

  return (
    <div className="main-flex-container">
      <div className="form-notification-container">
        {notification}
      </div>
      {noAccount
        ? <Register notifySuccess={handleRegisterSuccess}/>
        : <Login />
      }
      <button type="button" onClick={() => noAccount ? setNoAccount(false) : setNoAccount(true)}>
        {noAccount ? "Zu Login wechseln" : "Zu Registrieren wechseln"}
      </button>
    </div>
  )

}
