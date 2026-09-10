import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider, homeRouteForRole, useAuth } from "./auth/AuthContext";
import { ProtectedRoute } from "./auth/ProtectedRoute";
import { ToastProvider } from "./components/ui/ToastProvider";
import { AppLayout } from "./components/layout/AppLayout";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { ForbiddenPage } from "./pages/ForbiddenPage";
import { DashboardHomePage } from "./pages/dashboard/DashboardHomePage";
import { ClientNotificationsPage } from "./pages/client/ClientNotificationsPage";
import { ProduitsPage } from "./pages/produits/ProduitsPage";
import { VentesPage } from "./pages/ventes/VentesPage";
import { CommandesPage } from "./pages/commandes/CommandesPage";
import { FournisseursPage } from "./pages/fournisseurs/FournisseursPage";
import { PharmaciensPage } from "./pages/pharmaciens/PharmaciensPage";
import { RapportsPage } from "./pages/rapports/RapportsPage";

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false } }
});

function RootRedirect() {
  const { user } = useAuth();
  return <Navigate to={user ? homeRouteForRole(user.role) : "/login"} replace />;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/403" element={<ForbiddenPage />} />

      <Route element={<ProtectedRoute allowedRoles={["CLIENT"]} />}>
        <Route element={<AppLayout />}>
          <Route path="/client" element={<ClientNotificationsPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRoles={["PHARMACIEN", "RESPONSABLE"]} />}>
        <Route element={<AppLayout />}>
          <Route path="/pharmacien" element={<DashboardHomePage />} />
          <Route path="/responsable" element={<DashboardHomePage />} />
          <Route path="/produits" element={<ProduitsPage />} />
          <Route path="/ventes" element={<VentesPage />} />
          <Route path="/commandes" element={<CommandesPage />} />
          <Route path="/fournisseurs" element={<FournisseursPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRoles={["RESPONSABLE"]} />}>
        <Route element={<AppLayout />}>
          <Route path="/pharmaciens" element={<PharmaciensPage />} />
          <Route path="/rapports" element={<RapportsPage />} />
        </Route>
      </Route>

      <Route path="/" element={<RootRedirect />} />
      <Route path="*" element={<RootRedirect />} />
    </Routes>
  );
}

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
        <BrowserRouter>
          <AuthProvider>
            <AppRoutes />
          </AuthProvider>
        </BrowserRouter>
      </ToastProvider>
    </QueryClientProvider>
  );
}
