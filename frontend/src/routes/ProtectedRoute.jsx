import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
export function ProtectedRoute() {
  return useAuth().user ? <Outlet /> : <Navigate to="/login" replace />;
}
export function RoleRoute() {
  return useAuth().isAdmin ? (
    <Outlet />
  ) : (
    <Navigate to="/sem-permissao" replace />
  );
}
