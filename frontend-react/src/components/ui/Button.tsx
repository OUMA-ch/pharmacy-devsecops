import { type ButtonHTMLAttributes } from "react";
import clsx from "clsx";

type Variant = "primary" | "success" | "secondary" | "danger" | "link";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
}

const VARIANT_CLASSES: Record<Variant, string> = {
  // Bouton d'action primaire (ex: "Valider" en bleu dans les modales)
  primary: "bg-blue-600 text-white hover:bg-blue-700 disabled:bg-blue-300",
  // Bouton d'action de creation (ex: "Ajouter", "Se connecter")
  success: "bg-emerald-500 text-white hover:bg-emerald-600 disabled:bg-emerald-300",
  // Bouton neutre/secondaire (ex: "Cancel", "Actualiser")
  secondary: "bg-slate-100 text-slate-700 border border-slate-300 hover:bg-slate-200",
  // Bouton destructif (ex: "Supprimer")
  danger: "bg-red-600 text-white hover:bg-red-700 disabled:bg-red-300",
  link: "text-blue-600 hover:underline bg-transparent p-0"
};

export function Button({ variant = "secondary", className, disabled, ...props }: ButtonProps) {
  return (
    <button
      className={clsx(
        "rounded-md px-4 py-2 text-sm font-medium transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-500 disabled:cursor-not-allowed",
        VARIANT_CLASSES[variant],
        className
      )}
      disabled={disabled}
      {...props}
    />
  );
}
