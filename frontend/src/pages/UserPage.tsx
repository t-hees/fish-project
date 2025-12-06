import { useState } from "react"
import { fetchApi } from "../util/fetchApi";
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
  const [newPassword, setNewPassword] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(false);
  const [verificationContext, setVerificationContext] = useState<VerificationContext | null>(null);

  const changePasswordMessage = "Bestätige das alte Password zum Ändern";
  const deleteUserMessage = "Bestätige das Password um den Nutzer unwiderrufbar zu löschen";

  const deleteAccount: VerificationAction = (oldPassword: string) => {
    setLoading(true);
    const relPath = "user/delete";
    const jsonBody = {
      string: oldPassword,
    };
    fetchApi(relPath, "POST", handleAccountDeletionResponse, setError, setLoading, jsonBody);
  }

  const changePassword: VerificationAction = (oldPassword: string) => {
    setLoading(true);
    const relPath = "user/change-password";
    const jsonBody = {
      oldPassword: oldPassword,
      newPassword: newPassword,
    };
    fetchApi(relPath, "POST", handleResponse, setError, setLoading, jsonBody);
  }

  const handleResponse = async (response: Response) => {
    const message = await response.text();
    setLoading(false);
    setError(null);
    setNotification(message);
  }

  const handleAccountDeletionResponse = async (response: Response) => {
    const message = await response.text();
    setLoading(false);
    console.log(message);
    localStorage.clear();
    navigate("/login");
  }

  return (
    <>
      <h2>{localStorage.getItem("username")}</h2>
      {loading
        ? <Loading />
        : (
          verificationContext
            ? <UserActionVerification message={verificationContext.message} action={verificationContext.action} />
            : (
              <>
                <form onSubmit={(e) => {
                  e.preventDefault();
                  setVerificationContext({message: changePasswordMessage, action: changePassword})
                }}>
                  <label className="form-label">Neues password:</label>
                  <input
                    type="password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    required
                  />
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
    <form onSubmit={() => verificationContext.action(oldPassword)}>
        <label className="form-label">{verificationContext.message}</label>
        <input
          type="password"
          value={oldPassword}
          onChange={(e) => setOldPassword(e.target.value)}
          required
        />
        <button className="form-submit-button" type="submit">Senden</button>
      </form>
  )
}
