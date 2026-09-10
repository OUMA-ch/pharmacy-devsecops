import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "./AuthContext";
import type { Role } from "../types/api";

interface ProtectedRouteProps {
  allowedRoles: Role[];
}

/**
 * Garde de route RBAC. Un utilisateur non connecte est renvoye vers /login ; un
 * utilisateur connecte mais dont le role n'est pas autorise pour cette route est
 * renvoye vers /403 (jamais un simple masquage de menu : la route elle-meme est
 * protegee, meme en tapant l'URL directement).
 *
 * IMPORTANT : cette garde est une protection cote FRONTEND uniquement. Le backend
 * (SecurityConfig.java) n'applique aujourd'hui aucune autorisation par role sur ses
 * endpoints REST — voir README-SECURITY.md pour le detail de ce compromis.
 */
export function ProtectedRoute({ allowedRoles }: ProtectedRouteProps) {
  const { user } = useAuth();
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (!allowedRoles.includes(user.role)) {
    return <Navigate to="/403" replace />;
  }

  return <Outlet />;
}
