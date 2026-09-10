import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import { useAuth, homeRouteForRole } from "../auth/AuthContext";
import { ApiError } from "../api/client";
import { loginSchema, type LoginFormValues } from "../lib/validationSchemas";
import { Button } from "../components/ui/Button";

interface LoginLocationState {
  from?: { pathname: string };
  registered?: boolean;
}

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation() as { state?: LoginLocationState };
  const [serverError, setServerError] = useState<string | null>(null);
  const justRegistered = location.state?.registered === true;

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm<LoginFormValues>({ resolver: zodResolver(loginSchema) });

  async function onSubmit(values: LoginFormValues) {
    setServerError(null);
    try {
      const user = await login(values);
      const redirectTo = location.state?.from?.pathname ?? homeRouteForRole(user.role);
      navigate(redirectTo, { replace: true });
    } catch (err) {
      const message = err instanceof ApiError ? err.message : "Impossible de se connecter.";
      setServerError(message);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-md">
        <h1 className="mb-6 text-center text-2xl font-bold text-slate-800">Gestion de Pharmacie</h1>

        {justRegistered && (
          <p
            role="status"
            className="mb-4 rounded-md bg-emerald-50 px-3 py-2 text-sm text-emerald-700"
          >
            Compte créé, vous pouvez maintenant vous connecter.
          </p>
        )}

        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className="mb-4 flex items-center gap-3">
            <label
              htmlFor="login-email"
              className="w-32 shrink-0 text-sm font-medium text-slate-700"
            >
              Email:
            </label>
            <input
              id="login-email"
              type="email"
              autoComplete="username"
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              {...register("email")}
            />
          </div>
          {errors.email && (
            <p className="mb-3 -mt-2 text-xs text-red-600">{errors.email.message}</p>
          )}

          <div className="mb-4 flex items-center gap-3">
            <label
              htmlFor="login-password"
              className="w-32 shrink-0 text-sm font-medium text-slate-700"
            >
              Mot de passe:
            </label>
            <input
              id="login-password"
              type="password"
              autoComplete="current-password"
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              {...register("password")}
            />
          </div>
          {errors.password && (
            <p className="mb-3 -mt-2 text-xs text-red-600">{errors.password.message}</p>
          )}

          {serverError && (
            <p role="alert" className="mb-4 text-sm text-red-600">
              {serverError}
            </p>
          )}

          <Button type="submit" variant="success" className="w-full" disabled={isSubmitting}>
            Se connecter
          </Button>
        </form>

        <p className="mt-5 text-center text-sm">
          <Link to="/register" className="text-blue-600 hover:underline">
            Créer un compte client
          </Link>
        </p>
      </div>
    </div>
  );
}
