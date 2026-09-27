import { useState } from "react"
import { fetchApi } from "../util/fetchApi";
import { useAuth } from "../util/AuthContext";
import { Loading } from "../components/Loading";
import { useNavigate } from "react-router-dom";
import { NotifiableContainer } from "../components/NotifiableContainer";
import type { NotifiableContentContext } from "../components/NotifiableContainer";
import { useTranslation } from "../i18n/LanguageContext";

type VerificationAction = (oldPassword: string) => void;

type VerificationContext = {
  message: string,
  action: VerificationAction,
};

// Kept as a key instead of the text, so the message follows a language change
type PendingVerification = {
  messageKey: "confirmChangePassword" | "confirmDeleteAccount",
  action: VerificationAction,
};

export default function UserPage() {
  return (
    <NotifiableContainer MainContent={User} />
  );
}

const User = ({ setNotification, setError }: NotifiableContentContext) => {
  const navigate = useNavigate();
  const { authClear } = useAuth();
  const [newPassword, setNewPassword] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(false);
  const [verificationContext, setVerificationContext] = useState<PendingVerification | null>(null);
  const { t } = useTranslation();

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
    setNotification(t.userPage.passwordChanged);
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
      <h2>{t.userPage.title}</h2>
      {loading
        ? <Loading />
        : (
          verificationContext
            ? <UserActionVerification message={t.userPage[verificationContext.messageKey]}
                action={verify(verificationContext.action)} />
            : (
              <>
                <form onSubmit={(e) => {
                  e.preventDefault();
                  setVerificationContext({messageKey: "confirmChangePassword", action: changePassword})
                }}>
                  <label className="form-label">{t.userPage.newPassword}</label>
                  <div>
                    <input
                      type="password"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      required
                    />
                  </div>
                  <button className="form-submit-button" type="submit">{t.userPage.changePassword}</button>
                </form>
                <div>
                  <label className="form-label">{t.userPage.deleteAccountLabel}</label>
                  <button type="button"
                    onClick={() => setVerificationContext({messageKey: "confirmDeleteAccount", action: deleteAccount})}>
                    {t.userPage.deleteAccount}
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
  const { t } = useTranslation();

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
        <button className="form-submit-button" type="submit">{t.common.send}</button>
      </form>
  )
}
