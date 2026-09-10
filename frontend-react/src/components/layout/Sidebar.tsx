import { NavLink } from "react-router-dom";
import clsx from "clsx";
import { useAuth } from "../../auth/AuthContext";
import {
  PHARMACIEN_NAV_ITEMS,
  RESPONSABLE_ONLY_NAV_ITEMS,
  ROLE_ESPACE_LABEL,
  type NavItem
} from "../../lib/constants";

function NavButton({ item }: { item: NavItem }) {
  return (
    <NavLink
      to={item.to}
      className={({ isActive }) =>
        clsx(
          "block w-full rounded-md px-4 py-2.5 text-sm font-medium transition-colors",
          item.variant === "light"
            ? clsx(
                "bg-slate-100 text-slate-700 hover:bg-slate-200",
                isActive && "ring-2 ring-slate-400"
              )
            : clsx(
                "bg-slate-800 text-white hover:bg-slate-700",
                isActive && "bg-slate-700 ring-1 ring-slate-500"
              )
        )
      }
    >
      {item.label}
    </NavLink>
  );
}

/**
 * Sidebar fixe : logo + libelle d'espace selon le role, liens de navigation
 * (aucun lien pour CLIENT, qui n'a qu'un seul ecran), bouton Deconnexion toujours
 * colle en bas.
 */
export function Sidebar() {
  const { user, logout } = useAuth();
  if (!user) return null;

  const showNav = user.role === "PHARMACIEN" || user.role === "RESPONSABLE";

  return (
    <aside className="flex h-screen w-[280px] shrink-0 flex-col bg-slate-900 text-white">
      <div className="px-5 pb-4 pt-6">
        <h1 className="text-lg font-bold text-white">PharmaHOSS</h1>
        <p className="mt-0.5 text-xs text-slate-400">{ROLE_ESPACE_LABEL[user.role]}</p>
      </div>
      <hr className="border-slate-700" />

      {showNav && (
        <nav className="flex flex-1 flex-col gap-1.5 overflow-y-auto px-3 py-4">
          {PHARMACIEN_NAV_ITEMS.map((item) => (
            <NavButton key={item.to} item={item} />
          ))}

          {user.role === "RESPONSABLE" && (
            <>
              <hr className="my-2 border-slate-700" />
              {RESPONSABLE_ONLY_NAV_ITEMS.map((item) => (
                <NavButton key={item.to} item={item} />
              ))}
            </>
          )}
        </nav>
      )}

      {!showNav && <div className="flex-1" />}

      <div className="p-3">
        <button
          type="button"
          onClick={logout}
          className="w-full rounded-md bg-red-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-red-700"
        >
          Déconnexion
        </button>
      </div>
    </aside>
  );
}
