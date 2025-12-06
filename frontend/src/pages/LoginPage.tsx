import { useState } from "react";
import { Login } from "../components/Login.tsx";
import { Register } from "../components/Register.tsx";
import { NotifiableContainer, type NotifiableContentContext, type WrappedComponent } from "../components/NotifiableContainer.tsx";

export default function LoginPage() {
  const [noAccount, setNoAccount] = useState<boolean>(false);

  const LoginPageContent = ({ setNotification, setError }: NotifiableContentContext) => {
    const setNotificationWrapper = (notification: string) => {
      setNoAccount(false); setNotification(notification);
    }
    return (
      <>
        {noAccount
          ? <Register setNotification={setNotificationWrapper} setError={setError} />
          : <Login setNotification={setNotification} setError={setError} />
        }
      </>
    )
  }

  const OuterWrapper = ({ InnerComponent }: WrappedComponent) => {
    return (
    <div className="main-flex-container">
      <InnerComponent />
      <button type="button" onClick={() => noAccount ? setNoAccount(false) : setNoAccount(true)}>
        {noAccount ? "Zu Login wechseln" : "Zu Registrieren wechseln"}
      </button>
    </div>
    );
  }

  return(
    <NotifiableContainer MainContent={LoginPageContent} ContentWrapper={OuterWrapper} />
  );
}

