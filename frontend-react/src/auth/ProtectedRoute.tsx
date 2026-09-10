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
 * Cette garde reste une protection UX (evite un flash de contenu interdit / un
 * aller-retour reseau inutile) : l'autorisation reelle est appliquee cote serveur
 * par SecurityConfig.java (RBAC Spring Security sur chaque endpoint, base sur le
 * role signe dans le JWT), qui est la seule source de verite — voir README-SECURITY.md.
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
