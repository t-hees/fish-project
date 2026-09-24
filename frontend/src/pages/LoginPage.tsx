import { useCallback, useState } from "react";
import { Login } from "../components/Login.tsx";
import { Register } from "../components/Register.tsx";
import { NotifiableContainer, type NotifiableContentContext, type WrappedComponent } from "../components/NotifiableContainer.tsx";

export default function LoginPage() {
  const [noAccount, setNoAccount] = useState<boolean>(false);

  // Memoized on noAccount, so the forms keep their state and are only replaced when switching between them
  const LoginPageContent = useCallback(({ setNotification, setError }: NotifiableContentContext) => {
    const setNotificationWrapper = (notification: string) => {
      setNoAccount(false); setNotification(notification);
    }
    return noAccount
      ? <Register setNotification={setNotificationWrapper} setError={setError} />
      : <Login setNotification={setNotification} setError={setError} />;
  }, [noAccount]);

  const OuterWrapper = useCallback(({ children }: WrappedComponent) => {
    return (
    <div className="main-flex-container">
      {children}
      <button type="button" onClick={() => setNoAccount(!noAccount)}>
        {noAccount ? "Zu Login wechseln" : "Zu Registrieren wechseln"}
      </button>
    </div>
    );
  }, [noAccount]);

  return(
    <NotifiableContainer MainContent={LoginPageContent} ContentWrapper={OuterWrapper} />
  );
}
