import { createContext } from "react";

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

export const AuthContext = createContext<AuthType>({name: null, isAuthenticated: () => false,
  fetchName: () => {}, setName: () => {}, authClear: () => {}});
