import { Link } from "react-router-dom";
import {
  ArrowUpRight,
  Plus,
  ArrowDownLeft,
  Clock,
  Building2,
  DoorOpen,
  KeyRound,
  ChevronRight,
  AlertCircle,
  CalendarDays,
  Users,
  Receipt,
} from "lucide-react";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from "recharts";
import { useData } from "../hooks/data";
import { useAuth } from "../contexts/AuthContext";
import { galeriasService } from "../services/galeriasService";
import { salasService } from "../services/salasService";
import { pagamentosService } from "../services/pagamentosService";
import { alugueisService } from "../services/alugueisService";
import { contratosService } from "../services/contratosService";
import { gastosExtrasService } from "../services/gastosExtrasService";
import { usuariosService } from "../services/usuariosService";
import { PageHeader, State, Badge } from "../components/UI";
import { money, date } from "../utils/format";
export default function Dashboard() {
  const { isAdmin } = useAuth();
  const payments = useData("pagamentos", pagamentosService.list);
  const rents = useData("alugueis", alugueisService.list);
  const contracts = useData(["contratos", "near"], contratosService.near);
  const galleries = useData(["galerias", "dashboard"], galeriasService.list);
  const rooms = useData(
    ["salas", "dashboard", galleries.data?.map((g) => g.id).join(",")],
    async () => {
      const lists = await Promise.all(
        galleries.data.map((g) => salasService.list(g.id)),
      );
      return lists.flat();
    },
    galleries.isSuccess,
  );
  const expenses = useData(
    ["gastos", "dashboard", galleries.data?.map((g) => g.id).join(",")],
    async () => {
      const lists = await Promise.all(
        galleries.data.map((g) => gastosExtrasService.list(g.id)),
      );
      return lists.flat();
    },
    galleries.isSuccess,
  );
  const users = useData("usuarios", usuariosService.list, isAdmin);
  const metric = (q, fn) =>
    q.isError ? "Indisponível" : q.isSuccess ? fn(q.data) : "…";
  const paid = payments.data?.filter((p) => p.status === "PAGO") || [];
  const pending = payments.data?.filter((p) => p.status !== "PAGO") || [];
  const late = pending.filter((p) => p.status === "ATRASADO");
  const total = (items) => items.reduce((s, p) => s + Number(p.valor), 0);
  const series = Object.entries(
    (payments.data || []).reduce((acc, p) => {
      const key = p.competencia.slice(0, 7);
      acc[key] ||= { name: key, recebido: 0, pendente: 0 };
      acc[key][p.status === "PAGO" ? "recebido" : "pendente"] += Number(
        p.valor,
      );
      return acc;
    }, {}),
  )
    .sort(([a], [b]) => a.localeCompare(b))
    .slice(-6)
    .map(([, v]) => v);
  const donut = ["PAGO", "PENDENTE", "ATRASADO"].map((name, i) => ({
    name,
    value: payments.data?.filter((p) => p.status === name).length || 0,
    color: ["#3F7656", "#C2A468", "#C57D70"][i],
  }));
  const active = rents.data?.filter((r) => r.status === "ATIVO") || [];
  return (
    <>
      <PageHeader
        eyebrow={
          isAdmin
            ? "PAINEL ADMINISTRATIVO"
            : "UMA VISÃO MAIS CLARA DO SEU NEGÓCIO"
        }
        title={
          isAdmin
            ? "Tudo conectado. Tudo acompanhado."
            : "Seu negócio em perspectiva."
        }
        description={
          isAdmin
            ? "Acompanhe as contas e a operação disponível em todo o sistema."
            : "Acompanhe seus espaços, cuide das relações e planeje o próximo passo."
        }
        action={
          <Link className="btn" to="/alugueis">
            <Plus size={18} /> Novo aluguel
          </Link>
        }
      />
      <div className="overview-banner">
        <div>
          <span className="live-dot" /> SEU CENTRO DE CONTROLE
          <span className="banner-description">
            Mais organização. Mais espaço para crescer.
          </span>
        </div>
        <span>
          <CalendarDays size={16} />
          {new Date().toLocaleDateString("pt-BR", {
            day: "numeric",
            month: "long",
            year: "numeric",
          })}
        </span>
      </div>
      <section className="metrics">
        <Metric
          title="Valor recebido"
          value={metric(payments, (d) =>
            money(total(d.filter((p) => p.status === "PAGO"))),
          )}
          detail="Mensalidades pagas · todos os períodos"
          Icon={ArrowDownLeft}
          featured
        />
        <Metric
          title="A receber"
          value={metric(payments, (d) =>
            money(total(d.filter((p) => p.status !== "PAGO"))),
          )}
          detail="Pendentes e atrasados"
          Icon={Clock}
        />
        <Metric
          title={isAdmin ? "Usuários cadastrados" : "Galerias"}
          value={
            isAdmin
              ? metric(users, (d) => d.length)
              : metric(galleries, (d) => d.length)
          }
          detail={
            isAdmin
              ? "Contas disponíveis no sistema"
              : "Seus espaços comerciais"
          }
          Icon={isAdmin ? Users : Building2}
        />
        <Metric
          title="Aluguéis ativos"
          value={metric(
            rents,
            (d) => d.filter((r) => r.status === "ATIVO").length,
          )}
          detail="Relações em andamento"
          Icon={KeyRound}
        />
      </section>
      <div className="dashboard-grid">
        <section className="panel revenue-panel">
          <div className="panel-heading">
            <div>
              <h2>Recebimentos em perspectiva</h2>
              <p>Por competência · até seis períodos mais recentes</p>
            </div>
            <Link className="text-link" to="/pagamentos">
              Ver pagamentos <ArrowUpRight size={16} />
            </Link>
          </div>
          <State query={payments} empty="Seus recebimentos começam aqui.">
            <div className="chart-legend">
              <span>
                <i style={{ background: "#3F7656" }} />
                Recebido
              </span>
              <span>
                <i style={{ background: "#C2A468" }} />A receber
              </span>
            </div>
            <div className="revenue-chart">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart
                  data={series}
                  margin={{ left: 0, right: 12, top: 20, bottom: 0 }}
                >
                  <defs>
                    <linearGradient id="revenue" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#68947A" stopOpacity={0.3} />
                      <stop offset="100%" stopColor="#68947A" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid
                    strokeDasharray="3 4"
                    vertical={false}
                    stroke="var(--border)"
                  />
                  <XAxis
                    dataKey="name"
                    tick={{ fontSize: 11, fill: "var(--muted)" }}
                    tickLine={false}
                    axisLine={false}
                  />
                  <YAxis
                    width={58}
                    tickFormatter={(v) => (v >= 1000 ? `${v / 1000} mil` : v)}
                    tick={{ fontSize: 11, fill: "var(--muted)" }}
                    tickLine={false}
                    axisLine={false}
                  />
                  <Tooltip
                    formatter={money}
                    contentStyle={{
                      background: "var(--card)",
                      borderColor: "var(--border)",
                      borderRadius: 12,
                      color: "var(--text)",
                    }}
                  />
                  <Area
                    isAnimationActive={false}
                    type="monotone"
                    name="Recebido"
                    dataKey="recebido"
                    stroke="#3F7656"
                    strokeWidth={3}
                    fill="url(#revenue)"
                  />
                  <Area
                    isAnimationActive={false}
                    type="monotone"
                    name="A receber"
                    dataKey="pendente"
                    stroke="#C2A468"
                    strokeWidth={2}
                    fill="transparent"
                    strokeDasharray="5 5"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </State>
        </section>
        <section className="panel distribution">
          <div className="panel-heading">
            <div>
              <h2>Suas mensalidades</h2>
              <p>Distribuição por status</p>
            </div>
          </div>
          <State query={payments}>
            <div className="donut">
              <ResponsiveContainer width="100%" height={190}>
                <PieChart>
                  <Pie
                    isAnimationActive={false}
                    data={donut}
                    dataKey="value"
                    innerRadius={62}
                    outerRadius={80}
                    paddingAngle={4}
                    stroke="none"
                  >
                    {donut.map((d) => (
                      <Cell key={d.name} fill={d.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
              <div className="donut-center">
                <strong>{payments.data?.length}</strong>
                <span>mensalidades</span>
              </div>
            </div>
            <div className="donut-legend">
              {donut.map((d) => (
                <div key={d.name}>
                  <span>
                    <i style={{ background: d.color }} />
                    {d.name === "PAGO"
                      ? "Pagas"
                      : d.name === "PENDENTE"
                        ? "Pendentes"
                        : "Atrasadas"}
                  </span>
                  <strong>{d.value}</strong>
                </div>
              ))}
            </div>
          </State>
        </section>
      </div>
      <div className="dashboard-grid lower">
        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Precisa da sua atenção</h2>
              <p>Os próximos passos para manter tudo em dia.</p>
            </div>
            <span className="attention-icon">
              <AlertCircle size={19} />
            </span>
          </div>
          <State query={payments} empty="Nenhum pagamento para acompanhar.">
            <Link className="attention-row" to="/pagamentos">
              <span className="attention-icon">
                <Clock size={20} />
              </span>
              <div>
                <strong>
                  {late.length
                    ? `${late.length} pagamentos atrasados`
                    : "Pagamentos em dia"}
                </strong>
                <small>
                  {late.length
                    ? `${money(total(late))} aguardando recebimento`
                    : "Nenhuma mensalidade atrasada encontrada."}
                </small>
              </div>
              <ChevronRight size={17} />
            </Link>
          </State>
          <State
            query={contracts}
            empty="Nenhum contrato próximo do vencimento."
          >
            <Link className="attention-row" to="/contratos">
              <span className="attention-icon soft">
                <CalendarDays size={20} />
              </span>
              <div>
                <strong>
                  {contracts.data?.length} contratos próximos do vencimento
                </strong>
                <small>Confira os prazos e planeje o próximo período.</small>
              </div>
              <ChevronRight size={17} />
            </Link>
          </State>
          {!isAdmin && rooms.isSuccess && rents.isSuccess && (
            <Link className="attention-row" to="/salas">
              <span className="attention-icon soft">
                <DoorOpen size={20} />
              </span>
              <div>
                <strong>
                  {
                    rooms.data.filter(
                      (s) => !active.some((r) => r.salaId === s.id),
                    ).length
                  }{" "}
                  salas disponíveis
                </strong>
                <small>Novas oportunidades para seu negócio.</small>
              </div>
              <ChevronRight size={17} />
            </Link>
          )}
        </section>
        <section className="panel portfolio">
          <h2>{isAdmin ? "Acesso administrativo" : "Seu portfólio"}</h2>
          <p>
            {isAdmin
              ? "Visões disponíveis para sua conta."
              : "Uma leitura rápida dos seus espaços."}
          </p>
          <div className="portfolio-stats">
            <div>
              <span>Salas {isAdmin ? "próprias" : ""}</span>
              <strong>{metric(rooms, (d) => d.length)}</strong>
            </div>
            <div>
              <span>Aluguéis encerrados</span>
              <strong>
                {metric(
                  rents,
                  (d) => d.filter((r) => r.status === "ENCERRADO").length,
                )}
              </strong>
            </div>
            <div>
              <span>Gastos {isAdmin ? "próprios" : ""}</span>
              <strong>{metric(expenses, (d) => money(total(d)))}</strong>
            </div>
          </div>
          {isAdmin && (
            <p className="muted">
              Galerias, salas e gastos deste quadro consideram apenas suas
              galerias. Busque outros recursos nas páginas correspondentes.
            </p>
          )}
          <Link
            className="btn secondary"
            to={isAdmin ? "/admin/usuarios" : "/galerias"}
          >
            {isAdmin ? "Consultar usuários" : "Explorar minhas galerias"}
            <ArrowUpRight size={17} />
          </Link>
        </section>
      </div>
    </>
  );
}
function Metric({ title, value, detail, Icon, featured }) {
  return (
    <article className={"metric " + (featured ? "featured" : "")}>
      <div className="flex-between">
        <span>{title}</span>
        <span className="metric-icon">
          <Icon size={18} />
        </span>
      </div>
      <strong>{value}</strong>
      <small>{detail}</small>
    </article>
  );
}
