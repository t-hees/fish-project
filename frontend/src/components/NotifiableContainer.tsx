import { useState, type ReactNode } from "react";

export type NotifiableContentContext = {setNotification: React.Dispatch<string>, setError: React.Dispatch<string | null>}
export type WrappedComponent = {children: ReactNode};
type NotifiableContainerContext = {
  MainContent: React.ComponentType<NotifiableContentContext>,
  ContentWrapper?: React.ComponentType<WrappedComponent>,
}

/**
 * A component representing a container with notifications and error messages
 * @param mainContent The content inside of the containter
 * @param contentWrapper An optional wrapper arount the entire container. In this case the component
   just returns the contents without an outer div
 */
export function NotifiableContainer({ MainContent, ContentWrapper }: NotifiableContainerContext) {
  const [notification, setNotification] = useState<string>("");
  const [error, setError] = useState<string | null>(null);

  // Plain elements instead of a component defined here, which would be a new component type on every
  // render and remount MainContent (losing its state) whenever a notification or error is set
  const content = (
    <>
      <div className="form-notification-container">
        {notification}
      </div>
      <MainContent setNotification={setNotification} setError={setError}/>
      {error && <p className="form-error-container">{error}</p>}
    </>
  );

  return ContentWrapper
    ? <ContentWrapper>{content}</ContentWrapper>
    : <div className="main-flex-container">{content}</div>;
}
