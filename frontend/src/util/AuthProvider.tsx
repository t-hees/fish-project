import { useCallback, useState, type ReactNode } from "react";
import { fetchApi } from './fetchApi.ts'
import { AuthContext } from "./AuthContext.tsx";

export function AuthProvider({children}: {children: ReactNode}) {
  const [userName, setUserName] = useState<string|null>(null);

  const fetchName = useCallback(() => {
    fetchApi("user/name", "GET", async (response) => setUserName(await response.text()),
      (error) => console.error(error), () => {});
  }, []);

  const isAuthenticated = () => {
    return userName != null;
  }

  const clear = () => {
    setUserName(null);
  }

  const setName = (name: string) => {
    setUserName(name);
  }

  return (
    <AuthContext.Provider value={{name: userName, isAuthenticated: isAuthenticated,
      fetchName: fetchName, setName: setName, authClear: clear}}>
      {children}
    </AuthContext.Provider>
  )
}
