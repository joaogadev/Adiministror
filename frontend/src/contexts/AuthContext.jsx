import { createContext, useContext, useState, useEffect } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { jwtDecode } from "jwt-decode";
import { TOKEN_KEY } from "../services/api";
const Context = createContext();
export function readSession(token) {
  try {
    const p = jwtDecode(token);
    if (!p.sub || !p.exp || p.exp * 1000 <= Date.now()) return null;
    const roles = Array.isArray(p.role) ? p.role : [p.role];
    if (!roles.some((r) => ["DONO", "ADMINISTRADOR"].includes(r))) return null;
    return {
      id: p.sub,
      email: p.email,
      role: roles.includes("ADMINISTRADOR") ? "ADMINISTRADOR" : "DONO",
      exp: p.exp,
    };
  } catch {
    return null;
  }
}
export function AuthProvider({ children }) {
  const query = useQueryClient();
  const [user, setUser] = useState(() =>
    readSession(localStorage.getItem(TOKEN_KEY)),
  );
  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    query.clear();
    setUser(null);
  };
  useEffect(() => {
    const handler = () => logout();
    const sync = () => {
      query.clear();
      setUser(readSession(localStorage.getItem(TOKEN_KEY)));
    };
    window.addEventListener("session-expired", handler);
    window.addEventListener("storage", sync);
    return () => {
      window.removeEventListener("session-expired", handler);
      window.removeEventListener("storage", sync);
    };
  }, []);
  useEffect(() => {
    if (!user) return;
    const id = setTimeout(
      logout,
      Math.min(user.exp * 1000 - Date.now(), 2147483647),
    );
    return () => clearTimeout(id);
  }, [user]);
  const login = (token) => {
    const next = readSession(token);
    if (!next) throw new Error("Sessão inválida ou expirada.");
    query.clear();
    localStorage.setItem(TOKEN_KEY, token);
    setUser(next);
  };
  return (
    <Context.Provider
      value={{ user, login, logout, isAdmin: user?.role === "ADMINISTRADOR" }}
    >
      {children}
    </Context.Provider>
  );
}
export const useAuth = () => useContext(Context);
