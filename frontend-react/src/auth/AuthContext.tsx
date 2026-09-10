import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react";
import * as authApi from "../api/auth";
import type { LoginInput, Role, UserResponseDTO } from "../types/api";
import { clearStoredUser, readStoredUser, writeStoredUser } from "./storage";

interface AuthContextValue {
  user: UserResponseDTO | null;
  login: (input: LoginInput) => Promise<UserResponseDTO>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function homeRouteForRole(role: Role): string {
  switch (role) {
    case "CLIENT":
      return "/client";
    case "RESPONSABLE":
      return "/responsable";
    case "PHARMACIEN":
    default:
      return "/pharmacien";
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserResponseDTO | null>(() => readStoredUser());

  const login = useCallback(async (input: LoginInput) => {
    const result = await authApi.login(input);
    writeStoredUser(result);
    setUser(result);
    return result;
  }, []);

  const logout = useCallback(() => {
    clearStoredUser();
    setUser(null);
  }, []);

  const value = useMemo(() => ({ user, login, logout }), [user, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}
