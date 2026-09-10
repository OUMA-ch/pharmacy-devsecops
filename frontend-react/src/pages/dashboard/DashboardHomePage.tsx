import { useAuth } from "../../auth/AuthContext";
import { PageHeader } from "../../components/layout/PageHeader";
import { DASHBOARD_WELCOME_TEXT, ROLE_DISPLAY_LABEL } from "../../lib/constants";

export function DashboardHomePage() {
  const { user } = useAuth();
  if (!user || (user.role !== "PHARMACIEN" && user.role !== "RESPONSABLE")) return null;

  return (
    <div className="flex h-full flex-col">
      <PageHeader
        title="Tableau de bord"
        roleLabel={ROLE_DISPLAY_LABEL[user.role]}
        rightText="Bienvenue sur PharmaHOSS"
      />
      <div className="flex flex-1 items-center justify-center">
        <div className="w-full max-w-xl rounded-lg bg-white p-10 text-center shadow-md">
          <h2 className="text-2xl font-bold text-slate-800">Bienvenue</h2>
          <p className="mt-3 text-sm text-slate-500">{DASHBOARD_WELCOME_TEXT[user.role]}</p>
        </div>
      </div>
    </div>
  );
}
