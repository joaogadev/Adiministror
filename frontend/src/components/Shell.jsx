import { useEffect, useState, useRef } from "react";
import { NavLink, Outlet, useLocation, Link } from "react-router-dom";
import {
  LayoutDashboard,
  Building2,
  DoorOpen,
  Users,
  KeyRound,
  Wallet,
  FileText,
  Receipt,
  MessagesSquare,
  Settings,
  ShieldCheck,
  ArrowUpRight,
  Menu,
  X,
  LogOut,
  Sun,
  Moon,
} from "lucide-react";
import { driver } from "driver.js";
import "driver.js/dist/driver.css";
import { useAuth } from "../contexts/AuthContext";
import { useTheme } from "../contexts/ThemeContext";
export const navigation = [
  ["/", "Visão geral", LayoutDashboard],
  ["/galerias", "Galerias", Building2],
  ["/salas", "Salas", DoorOpen],
  ["/inquilinos", "Inquilinos", Users],
  ["/alugueis", "Aluguéis", KeyRound],
  ["/pagamentos", "Pagamentos", Wallet],
  ["/contratos", "Contratos", FileText],
  ["/gastos", "Gastos extras", Receipt],
  ["/assistente", "Assistente", MessagesSquare],
  ["/configuracoes", "Configurações", Settings],
];
export function startTour(user) {
  const admin = user.role === "ADMINISTRADOR";
  const key = `adiministror_onboarding_${user.id}_${user.role}`;
  const steps = [
    {
      popover: {
        title: admin
          ? "Seu painel administrativo"
          : "Boas-vindas ao Adiministror",
        description: admin
          ? "Acompanhe usuários e os recursos disponíveis para administração."
          : "Um espaço para cuidar das suas galerias, contratos e recebimentos.",
      },
    },
    ...(admin
      ? [
          {
            popover: {
              title: "Usuários",
              description:
                "Consulte as contas cadastradas em Usuários. A gestão disponível é somente de leitura.",
            },
          },
        ]
      : []),
    ...[
      "Visão geral|Acompanhe indicadores e o que precisa da sua atenção.",
      "Galerias e salas|Organize seus espaços. Abra uma galeria para gerenciar suas salas.",
      "Aluguéis e inquilinos|Use Novo aluguel para vincular um inquilino, criar contrato e gerar a primeira mensalidade.",
      "Pagamentos|Registre recebimentos, altere vencimentos e gere mensalidades.",
      "Contratos e gastos|Consulte prazos, histórico e despesas das galerias.",
      "Configurações|Escolha o tema, refaça este tutorial e encerre sua sessão.",
    ].map((s) => {
      const [title, description] = s.split("|");
      const path = title.startsWith("Visão")
        ? "/"
        : title.startsWith("Galerias")
          ? "/galerias"
          : title.startsWith("Aluguéis")
            ? "/alugueis"
            : title === "Pagamentos"
              ? "/pagamentos"
              : title.startsWith("Contratos")
                ? "/contratos"
                : "/configuracoes";
      return {
        ...(window.innerWidth > 900
          ? { element: `nav a[href="${path}"]` }
          : {}),
        popover: { title, description },
      };
    }),
  ];
  const d = driver({
    showProgress: true,
    nextBtnText: "Próximo",
    prevBtnText: "Voltar",
    doneBtnText: "Começar",
    steps,
    onDestroyed: () => localStorage.setItem(key, "true"),
  });
  d.drive();
  return d;
}
export default function Shell() {
  const { user, isAdmin, logout } = useAuth();
  const { theme, setTheme } = useTheme();
  const [open, setOpen] = useState(false);
  const [mobile, setMobile] = useState(() => window.innerWidth <= 900);
  const sidebarRef = useRef(null);
  useEffect(() => {
    const mq = matchMedia("(max-width: 900px)");
    const update = () => {
      setMobile(mq.matches);
      if (!mq.matches) setOpen(false);
    };
    mq.addEventListener("change", update);
    return () => mq.removeEventListener("change", update);
  }, []);
  const location = useLocation();
  useEffect(() => setOpen(false), [location.pathname]);
  useEffect(() => {
    const key = `adiministror_onboarding_${user.id}_${user.role}`;
    let tour;
    const t = setTimeout(() => {
      if (!localStorage.getItem(key)) tour = startTour(user);
    }, 500);
    return () => {
      clearTimeout(t);
      tour?.destroy();
    };
  }, [user.id]);
  useEffect(() => {
    if (!open) return;
    const previous = document.activeElement;
    const oldOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    sidebarRef.current?.querySelector("a,button")?.focus();
    const close = (e) => {
      if (e.key === "Escape") setOpen(false);
      if (e.key === "Tab") {
        const list = [
          ...sidebarRef.current.querySelectorAll("a,button"),
        ].filter((el) => el.offsetWidth > 0);
        const first = list[0],
          last = list.at(-1);
        if (e.shiftKey && document.activeElement === first) {
          e.preventDefault();
          last.focus();
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault();
          first.focus();
        }
      }
    };
    document.addEventListener("keydown", close);
    return () => {
      document.body.style.overflow = oldOverflow;
      document.removeEventListener("keydown", close);
      previous?.focus();
    };
  }, [open]);
  const links = isAdmin
    ? [
        navigation[0],
        ["/admin/usuarios", "Usuários", ShieldCheck],
        ...navigation.slice(1),
      ]
    : navigation;
  return (
    <div className="app-shell">
      {open && (
        <button
          className="sidebar-backdrop"
          aria-label="Fechar menu"
          onClick={() => setOpen(false)}
        />
      )}
      <aside
        ref={sidebarRef}
        inert={mobile && !open}
        className={"sidebar " + (open ? "open" : "")}
      >
        <Link to="/" className="brand">
          <span className="brand-symbol">
            <Building2 size={23} />
          </span>
          Adiministror<span className="brand-dot">.</span>
        </Link>
        <button
          className="icon-btn mobile-close"
          aria-label="Fechar menu"
          onClick={() => setOpen(false)}
        >
          <X />
        </button>
        <div className="workspace">
          <span className="workspace-icon">
            {isAdmin ? <ShieldCheck size={21} /> : <Building2 size={21} />}
          </span>
          <div>
            <strong>{isAdmin ? "Administração" : "Meu negócio"}</strong>
            <small>
              {isAdmin ? "Visão administrativa" : "Gestão de galerias"}
            </small>
          </div>
        </div>
        <span className="nav-label">PRINCIPAL</span>
        <nav>
          {links.map(([path, label, Icon]) => (
            <NavLink
              key={path}
              to={path}
              end={path === "/"}
              className={({ isActive }) =>
                isActive ? "nav-item active" : "nav-item"
              }
            >
              <Icon size={19} />
              <span>{label}</span>
              {path === "/assistente" && <small className="soon">PRÉVIA</small>}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-tip">
          <span className="eyebrow">TUDO NO SEU LUGAR</span>
          <p>Mais clareza para o seu próximo passo.</p>
          <Link to="/alugueis">
            Gerenciar aluguéis <ArrowUpRight size={16} />
          </Link>
        </div>
        <div className="sidebar-profile">
          <div className="avatar">{user.email?.[0]?.toUpperCase() || "A"}</div>
          <div>
            <strong>{isAdmin ? "Administrador" : "Proprietário"}</strong>
            <small>{user.email}</small>
          </div>
          <button className="icon-btn" aria-label="Sair" onClick={logout}>
            <LogOut size={17} />
          </button>
        </div>
      </aside>
      <div className="main-wrap">
        <header className="topbar">
          <div className="topbar-left">
            <button
              className="icon-btn mobile-menu"
              aria-label="Abrir menu"
              onClick={() => setOpen(true)}
            >
              <Menu />
            </button>
            <span className="topbar-mark">Workspace</span>
            <span className="divider">/</span>
            <strong>
              {links.find(([p]) =>
                p === "/"
                  ? location.pathname === "/"
                  : location.pathname.startsWith(p),
              )?.[1] || "Detalhes"}
            </strong>
          </div>
          <div className="topbar-right">
            <span className="profile-badge">
              {isAdmin ? "Administrador" : "Dono"}
            </span>
            <button
              className="icon-btn"
              aria-label="Alternar tema"
              onClick={() =>
                setTheme(
                  document.documentElement.dataset.theme === "dark"
                    ? "light"
                    : "dark",
                )
              }
            >
              {theme === "dark" ? <Sun size={19} /> : <Moon size={19} />}
            </button>
            <Link
              to="/configuracoes"
              className="avatar"
              aria-label="Configurações da conta"
            >
              {user.email?.[0]?.toUpperCase() || "A"}
            </Link>
          </div>
        </header>
        <main>
          <Outlet />
        </main>
        <footer>
          Adiministror <span>Seu negócio, sob uma nova perspectiva.</span>
        </footer>
      </div>
    </div>
  );
}
