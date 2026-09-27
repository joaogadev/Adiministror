import { lazy, Suspense } from "react";
import { Routes, Route, Link } from "react-router-dom";
import Shell from "./components/Shell";
import { ProtectedRoute, RoleRoute } from "./routes/ProtectedRoute";
const Auth = lazy(() => import("./pages/Auth"));
const Dashboard = lazy(() => import("./pages/Dashboard"));
const Galerias = lazy(() => import("./pages/Galerias"));
const GaleriaDetail = lazy(() =>
  import("./pages/Galerias").then((m) => ({ default: m.GaleriaDetail })),
);
const Salas = lazy(() => import("./pages/Salas"));
const Inquilinos = lazy(() => import("./pages/Inquilinos"));
const Alugueis = lazy(() => import("./pages/Alugueis"));
const Pagamentos = lazy(() => import("./pages/Pagamentos"));
const Contratos = lazy(() => import("./pages/Contratos"));
const Gastos = lazy(() => import("./pages/Gastos"));
const Usuarios = lazy(() => import("./pages/Usuarios"));
const Assistente = lazy(() => import("./pages/Assistente"));
const Configuracoes = lazy(() => import("./pages/Configuracoes"));
export default function App() {
  return (
    <Suspense
      fallback={
        <div className="state" role="status">
          Carregando seu espaço…
        </div>
      }
    >
      <Routes>
        <Route path="/login" element={<Auth />} />
        <Route path="/registro" element={<Auth register />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<Shell />}>
            <Route index element={<Dashboard />} />
            <Route path="galerias" element={<Galerias />} />
            <Route path="galerias/:id" element={<GaleriaDetail />} />
            <Route path="salas" element={<Salas />} />
            <Route path="inquilinos" element={<Inquilinos />} />
            <Route path="alugueis" element={<Alugueis />} />
            <Route path="pagamentos" element={<Pagamentos />} />
            <Route path="contratos" element={<Contratos />} />
            <Route path="gastos" element={<Gastos />} />
            <Route path="assistente" element={<Assistente />} />
            <Route path="configuracoes" element={<Configuracoes />} />
            <Route element={<RoleRoute />}>
              <Route path="admin/usuarios" element={<Usuarios />} />
            </Route>
            <Route
              path="sem-permissao"
              element={
                <div className="state">
                  <h1>Acesso restrito</h1>
                  <p>Você não possui permissão para acessar esta página.</p>
                  <Link className="btn" to="/">
                    Voltar ao início
                  </Link>
                </div>
              }
            />
          </Route>
        </Route>
        <Route
          path="*"
          element={
            <div className="state">
              <h1>Página não encontrada</h1>
              <Link className="btn" to="/">
                Voltar ao início
              </Link>
            </div>
          }
        />
      </Routes>
    </Suspense>
  );
}
