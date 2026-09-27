import { Link, Navigate, useNavigate } from "react-router-dom";
import { Building2, ArrowUpRight, ShieldCheck, Check } from "lucide-react";
import { toast } from "sonner";
import { Form } from "../components/UI";
import { authService } from "../services/authService";
import { useAuth } from "../contexts/AuthContext";
export default function Auth({ register = false }) {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  if (user) return <Navigate to="/" replace />;
  const fields = [
    ...(register
      ? [
          {
            name: "nome",
            label: "Nome completo",
            minLength: 3,
            maxLength: 255,
          },
          {
            name: "phone",
            label: "Telefone",
            type: "tel",
            pattern: /^\+?[0-9]{10,15}$/,
            placeholder: "79999999999",
          },
        ]
      : []),
    { name: "email", label: "E-mail", type: "email", autoComplete: "email" },
    {
      name: "senha",
      label: "Senha",
      type: "password",
      autoComplete: register ? "new-password" : "current-password",
    },
  ];
  return (
    <div className="auth-page">
      <section className="auth-story">
        <div className="brand">
          <span className="brand-symbol">
            <Building2 />
          </span>
          Adiministror.
        </div>
        <div className="auth-copy">
          <span className="eyebrow">ESPAÇO PARA CRESCER</span>
          <h1>
            Seu patrimônio.
            <br />
            Sua visão.
            <br />
            <em>Tudo conectado.</em>
          </h1>
          <p>
            Uma gestão mais simples para quem transforma espaços em
            oportunidades.
          </p>
          <div className="building-art" aria-hidden="true">
            {[1, 2, 3].map((n) => (
              <div className={"building b" + n} key={n}>
                {Array.from({ length: 12 }, (_, i) => (
                  <i key={i} />
                ))}
              </div>
            ))}
          </div>
        </div>
        <div className="auth-bottom">
          <ShieldCheck size={18} /> Galerias, aluguéis e finanças em um só
          lugar.
        </div>
      </section>
      <section className="auth-form">
        <div className="auth-form-inner">
          <span className="eyebrow">BEM-VINDO AO SEU PRÓXIMO CAPÍTULO</span>
          <h2>{register ? "Comece sua gestão" : "Bom ter você por aqui."}</h2>
          <p>
            {register
              ? "Crie sua conta de proprietário e organize seu negócio."
              : "Entre para acompanhar o que importa para o seu negócio."}
          </p>
          <Form
            key={String(register)}
            fields={fields}
            submit={register ? "Criar minha conta" : "Entrar no Adiministror"}
            onSubmit={async (data) => {
              if (register) {
                await authService.register(data);
                toast.success("Conta criada! Entre para começar.");
                navigate("/login");
              } else {
                const r = await authService.login(data);
                login(r.accessToken);
                navigate("/");
              }
            }}
          />
          <p className="auth-switch">
            {register ? "Já tem uma conta?" : "Ainda não tem uma conta?"}{" "}
            <Link to={register ? "/login" : "/registro"}>
              {register ? "Entrar" : "Criar conta"} <ArrowUpRight size={15} />
            </Link>
          </p>
          <div className="auth-note">
            <Check size={16} /> Cada espaço tem potencial. Comece a cuidar do
            seu.
          </div>
        </div>
      </section>
    </div>
  );
}
