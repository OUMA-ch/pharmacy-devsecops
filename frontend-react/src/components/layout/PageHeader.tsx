interface PageHeaderProps {
  title: string;
  roleLabel?: string;
  rightText?: string;
}

/**
 * En-tete de zone de contenu : titre a gauche (avec "• Role" sur le tableau de
 * bord), texte optionnel a droite (ex. "Bienvenue sur PharmaHOSS").
 */
export function PageHeader({ title, roleLabel, rightText }: PageHeaderProps) {
  return (
    <div className="mb-6 flex items-center justify-between border-b border-slate-200 pb-4">
      <h1 className="text-xl font-semibold text-slate-800">
        {title}
        {roleLabel && <span className="ml-2 font-normal text-slate-400">• {roleLabel}</span>}
      </h1>
      {rightText && <p className="text-sm text-slate-500">{rightText}</p>}
    </div>
  );
}
