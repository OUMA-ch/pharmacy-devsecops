import type { ReactNode } from "react";

interface FormFieldProps {
  label: string;
  htmlFor: string;
  error?: string;
  /** Aide affichee sous le champ tant qu'il n'y a pas d'erreur (ex: regle de mot de passe). */
  hint?: string;
  children: ReactNode;
  required?: boolean;
}

/** Label au-dessus du champ + message d'erreur de validation sous le champ. */
export function FormField({ label, htmlFor, error, hint, children, required }: FormFieldProps) {
  return (
    <div className="mb-4">
      <label htmlFor={htmlFor} className="mb-1 block text-sm font-medium text-slate-700">
        {label}
        {required && <span className="text-red-500"> *</span>}
      </label>
      {children}
      {error ? (
        <p className="mt-1 text-xs text-red-600" role="alert">
          {error}
        </p>
      ) : (
        hint && <p className="mt-1 text-xs text-slate-500">{hint}</p>
      )}
    </div>
  );
}

export const inputClassName =
  "w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500";
