import { createContext, useState, type ReactNode } from "react";
import { fetchApi } from './fetchApi.ts'

type AuthType = {
  name: String|null,
  isAuthenticated: () => boolean,
  fetchName: () => void,
  setName: (name: string) => void,
  authClear: () => void
}


export const AuthContext = createContext<AuthType>({name: null, isAuthenticated: () => false,
  fetchName: () => {}, setName: () => {}, authClear: () => {}});

export function AuthProvider({children}: {children: ReactNode}) {
  const [userName, setUserName] = useState<String|null>(null);

  const fetchName = () => {
    fetchApi("user/name", "GET", async (response) => setUserName(await response.text()),
      (error) => console.error(error), () => console.log("fetching username"));
  }

  const isAuthenticated = () => {
    return userName != null;
  }

  const clear = () => {
    setUserName(null);
  }

  const setName = (name: String) => {
    setUserName(name);
  }

  return (
    <AuthContext.Provider value={{name: userName, isAuthenticated: isAuthenticated,
      fetchName: fetchName, setName: setName, authClear: clear}}>
      {children}
    </AuthContext.Provider>
  )
}
