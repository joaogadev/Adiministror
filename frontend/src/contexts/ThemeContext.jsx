import { createContext, useContext, useState, useEffect } from "react";
const Context = createContext();
export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(
    () => localStorage.getItem("adiministror_theme") || "system",
  );
  useEffect(() => {
    const media = matchMedia("(prefers-color-scheme: dark)");
    const apply = () =>
      (document.documentElement.dataset.theme =
        theme === "system" ? (media.matches ? "dark" : "light") : theme);
    apply();
    localStorage.setItem("adiministror_theme", theme);
    media.addEventListener("change", apply);
    return () => media.removeEventListener("change", apply);
  }, [theme]);
  return (
    <Context.Provider value={{ theme, setTheme }}>{children}</Context.Provider>
  );
}
export const useTheme = () => useContext(Context);
