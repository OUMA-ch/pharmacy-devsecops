import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import * as authApi from "../api/auth";
import { ApiError } from "../api/client";
import { PASSWORD_RULE, registerSchema, type RegisterFormValues } from "../lib/validationSchemas";
import { Button } from "../components/ui/Button";
import { FormField, inputClassName } from "../components/ui/FormField";

export function RegisterPage() {
  const navigate = useNavigate();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting }
  } = useForm<RegisterFormValues>({ resolver: zodResolver(registerSchema) });

  async function onSubmit(values: RegisterFormValues) {
    setServerError(null);
    try {
      // Le role n'est jamais envoye : UserService.registerClient() le force a
      // CLIENT cote serveur, quelle que soit la valeur eventuellement fournie.
      await authApi.registerClient(values);
      navigate("/login", { state: { registered: true } });
    } catch (err) {
      // 400 de validation backend : message affiche sous chaque champ concerne.
      let erreurSousUnChamp = false;
      if (err instanceof ApiError && err.fieldErrors) {
        for (const [champ, message] of Object.entries(err.fieldErrors)) {
          if (champ in registerSchema.shape) {
            setError(champ as keyof RegisterFormValues, { type: "server", message });
            erreurSousUnChamp = true;
          }
        }
      }
      if (!erreurSousUnChamp) {
        setServerError(err instanceof ApiError ? err.message : "Impossible de créer le compte.");
      }
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-md">
        <h1 className="text-center text-2xl font-bold text-slate-800">Créer un compte</h1>
        <p className="mt-1 text-center text-xs text-slate-400">
          Inscription réservée aux clients (CLIENT).
        </p>
        <hr className="my-4 border-slate-200" />

        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <FormField
            label="Nom complet"
            htmlFor="register-nom"
            error={errors.nomUser?.message}
            required
          >
            <input
              id="register-nom"
              type="text"
              className={inputClassName}
              {...register("nomUser")}
            />
          </FormField>

          <FormField label="Email" htmlFor="register-email" error={errors.email?.message} required>
            <input
              id="register-email"
              type="email"
              placeholder="Ex: client@test.com"
              className={inputClassName}
              {...register("email")}
            />
          </FormField>

          <FormField
            label="Téléphone"
            htmlFor="register-tele"
            error={errors.tele?.message}
            required
          >
            <input
              id="register-tele"
              type="tel"
              placeholder="Ex: 0612345678"
              className={inputClassName}
              {...register("tele")}
            />
          </FormField>

          <FormField
            label="Mot de passe"
            htmlFor="register-password"
            error={errors.password?.message}
            hint={PASSWORD_RULE}
            required
          >
            <input
              id="register-password"
              type="password"
              className={inputClassName}
              {...register("password")}
            />
          </FormField>

          {serverError && (
            <p role="alert" className="mb-4 text-sm text-red-600">
              {serverError}
            </p>
          )}

          <Button
            type="submit"
            variant="primary"
            className="w-full bg-blue-800 hover:bg-blue-900"
            disabled={isSubmitting}
          >
            S'inscrire
          </Button>
        </form>

        <p className="mt-5 text-center text-sm text-slate-500">
          Déjà un compte ?{" "}
          <Link to="/login" className="text-blue-600 hover:underline">
            Se connecter
          </Link>
        </p>
      </div>
    </div>
  );
}
