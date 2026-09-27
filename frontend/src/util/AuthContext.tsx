import { createContext, useContext } from "react";

export type UserInfo = {
  username: string,
}

export type AuthType = {
  name: string|null,
  isAuthenticated: () => boolean,
  fetchName: () => void,
  setName: (name: string) => void,
  authClear: () => void
}

export const AuthContext = createContext<AuthType>({
  name: null,
  isAuthenticated: () => false,
  fetchName: () => {},
  setName: () => {},
  authClear: () => {}
});

export function useAuth(): AuthType {
  const ctx = useContext(AuthContext);
  // if (!ctx) throw new Error("useAuth must be used with an AuthProvider!");
  return ctx;
}
