import { Sun, Moon, Monitor, BookOpen, LogOut } from "lucide-react";
import { PageHeader, Detail } from "../components/UI";
import { useAuth } from "../contexts/AuthContext";
import { useTheme } from "../contexts/ThemeContext";
import { startTour } from "../components/Shell";
export default function Configuracoes() {
  const { user, logout } = useAuth();
  const { theme, setTheme } = useTheme();
  return (
    <>
      <PageHeader
        title="Do seu jeito."
        description="Pequenas preferências para uma experiência que combina com você."
      />
      <section className="panel settings-section">
        <h2>Aparência</h2>
        <p>Escolha como quer ver seu espaço de trabalho.</p>
        <div className="theme-options">
          {[
            ["light", "Claro", Sun],
            ["dark", "Escuro", Moon],
            ["system", "Seguir sistema", Monitor],
          ].map(([value, label, Icon]) => (
            <button
              key={value}
              className={"theme-option " + (theme === value ? "selected" : "")}
              onClick={() => setTheme(value)}
              aria-pressed={theme === value}
            >
              <div className={"theme-preview " + value}>
                <i />
                <div>
                  <b />
                  <b />
                </div>
              </div>
              <span>
                <Icon size={17} />
                {label}
              </span>
            </button>
          ))}
        </div>
      </section>
      <section className="panel settings-section">
        <h2>Um novo passeio</h2>
        <p>Reveja as funcionalidades disponíveis para seu perfil.</p>
        <button className="btn secondary" onClick={() => startTour(user)}>
          <BookOpen size={17} /> Refazer tutorial
        </button>
      </section>
      <section className="panel settings-section">
        <h2>Sua sessão</h2>
        <Detail data={{ Email: user.email, Perfil: user.role }} />
        <button className="btn secondary" onClick={logout}>
          <LogOut size={17} /> Sair da conta
        </button>
      </section>
    </>
  );
}
