import { Link } from "react-router-dom";
import { useAuth, homeRouteForRole } from "../auth/AuthContext";

export function ForbiddenPage() {
  const { user } = useAuth();
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-slate-50 px-4 text-center">
      <span className="text-5xl">🚫</span>
      <h1 className="text-2xl font-bold text-slate-800">Accès refusé</h1>
      <p className="max-w-md text-sm text-slate-500">
        Vous n'avez pas les autorisations nécessaires pour accéder à cette page.
      </p>
      <Link
        to={user ? homeRouteForRole(user.role) : "/login"}
        className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
      >
        Retour au tableau de bord
      </Link>
    </div>
  );
}
