import { useContext, useState } from "react"
import { fetchApi } from "../util/fetchApi";
import { AuthContext } from "../util/AuthContext";
import { Loading } from "../components/Loading";
import { useNavigate } from "react-router-dom";
import { NotifiableContainer } from "../components/NotifiableContainer";
import type { NotifiableContentContext } from "../components/NotifiableContainer";

type VerificationAction = (oldPassword: string) => void;

type VerificationContext = {
  message: string,
  action: VerificationAction,
};

export default function UserPage() {
  return (
    <NotifiableContainer MainContent={User} />
  );
}

const User = ({ setNotification, setError }: NotifiableContentContext) => {
  const navigate = useNavigate();
  const { authClear } = useContext(AuthContext);
  const [newPassword, setNewPassword] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(false);
  const [verificationContext, setVerificationContext] = useState<VerificationContext | null>(null);

  const changePasswordMessage = "Bestätige das alte Password zum Ändern";
  const deleteUserMessage = "Bestätige das Password um den Nutzer unwiderrufbar zu löschen";

  const deleteAccount: VerificationAction = (oldPassword: string) => {
    setLoading(true);
    const jsonBody = {
      password: oldPassword,
    };
    fetchApi("users/me", "DELETE", handleAccountDeletionResponse, setError, setLoading, jsonBody);
  }

  const changePassword: VerificationAction = (oldPassword: string) => {
    setLoading(true);
    const jsonBody = {
      oldPassword: oldPassword,
      newPassword: newPassword,
    };
    fetchApi("users/me/password", "PUT", handleResponse, setError, setLoading, jsonBody);
  }

  const handleResponse = async () => {
    setLoading(false);
    setError(null);
    setNotification("Passwort geändert");
  }

  const handleAccountDeletionResponse = async () => {
    setLoading(false);
    authClear();
    navigate("/login");
  }

  const verify = (action: VerificationAction) => (oldPassword: string) => {
    setVerificationContext(null);
    action(oldPassword);
  }

  return (
    <>
      <h2>Nutzerdaten Ändern</h2>
      {loading
        ? <Loading />
        : (
          verificationContext
            ? <UserActionVerification message={verificationContext.message} action={verify(verificationContext.action)} />
            : (
              <>
                <form onSubmit={(e) => {
                  e.preventDefault();
                  setVerificationContext({message: changePasswordMessage, action: changePassword})
                }}>
                  <label className="form-label">Neues password:</label>
                  <div>
                    <input
                      type="password"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      required
                    />
                  </div>
                  <button className="form-submit-button" type="submit">Password ändern</button>
                </form>
                <div>
                  <label className="form-label">Account löschen:</label>
                  <button type="button"
                    onClick={() => setVerificationContext({message: deleteUserMessage, action: deleteAccount})}>
                    Account löschen
                  </button>
                </div>
              </>
            )
        )
      }
    </>
  )
}

function UserActionVerification(verificationContext: VerificationContext) {
  const [oldPassword, setOldPassword] = useState<string>("");

  return (
    <form onSubmit={(e) => {
      e.preventDefault();
      verificationContext.action(oldPassword);
    }}>
        <label className="form-label">{verificationContext.message}</label>
        <div>
          <input
            type="password"
            value={oldPassword}
            onChange={(e) => setOldPassword(e.target.value)}
            required
          />
        </div>
        <button className="form-submit-button" type="submit">Senden</button>
      </form>
  )
}
