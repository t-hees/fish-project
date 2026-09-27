import { useCallback, useState } from "react";
import { Login } from "../components/Login.tsx";
import { Register } from "../components/Register.tsx";
import { NotifiableContainer, type NotifiableContentContext, type WrappedComponent } from "../components/NotifiableContainer.tsx";
import { useTranslation } from "../i18n/LanguageContext.ts";

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
        <SwitchFormText noAccount={noAccount} />
      </button>
    </div>
    );
  }, [noAccount]);

  return(
    <NotifiableContainer MainContent={LoginPageContent} ContentWrapper={OuterWrapper} />
  );
}

// Translated in its own component, so a language change doesn't recreate OuterWrapper and reset the forms
function SwitchFormText({ noAccount }: { noAccount: boolean }) {
  const { t } = useTranslation();
  return noAccount ? t.loginPage.switchToLogin : t.loginPage.switchToRegister;
}
