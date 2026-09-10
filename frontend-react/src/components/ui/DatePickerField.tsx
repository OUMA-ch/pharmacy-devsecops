import { useEffect, useRef, useState } from "react";
import clsx from "clsx";
import { inputClassName } from "./FormField";

interface DatePickerFieldProps {
  id: string;
  value: string; // format YYYY-MM-DD, chaine vide si non renseigne
  onChange: (value: string) => void;
  placeholder?: string;
}

const WEEKDAYS = ["Lu", "Ma", "Me", "Je", "Ve", "Sa", "Di"];
const MONTH_NAMES = [
  "Janvier",
  "Fevrier",
  "Mars",
  "Avril",
  "Mai",
  "Juin",
  "Juillet",
  "Aout",
  "Septembre",
  "Octobre",
  "Novembre",
  "Decembre"
];

function toIsoDate(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

function parseIsoDate(value: string): Date | null {
  if (!value) return null;
  const parsed = new Date(`${value}T00:00:00`);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
}

/**
 * Champ date : input texte (format YYYY-MM-DD, saisissable directement) + icone
 * calendrier qui ouvre un mini calendrier mensuel avec navigation mois
 * precedent/suivant, comme observe dans l'app d'origine (modales Produit,
 * Ordonnance, Rapports). Composant maison, sans dependance externe.
 */
export function DatePickerField({ id, value, onChange, placeholder }: DatePickerFieldProps) {
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const selected = parseIsoDate(value);
  const [viewMonth, setViewMonth] = useState<Date>(selected ?? new Date());

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    }
    function handleEscape(event: KeyboardEvent) {
      if (event.key === "Escape") setOpen(false);
    }
    if (open) {
      document.addEventListener("mousedown", handleClickOutside);
      document.addEventListener("keydown", handleEscape);
    }
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("keydown", handleEscape);
    };
  }, [open]);

  const year = viewMonth.getFullYear();
  const month = viewMonth.getMonth();
  const firstOfMonth = new Date(year, month, 1);
  // Lundi = 0 ... Dimanche = 6
  const leadingBlanks = (firstOfMonth.getDay() + 6) % 7;
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const cells: (number | null)[] = [
    ...Array<null>(leadingBlanks).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1)
  ];

  function pickDay(day: number) {
    const picked = new Date(year, month, day);
    onChange(toIsoDate(picked));
    setOpen(false);
  }

  return (
    <div ref={containerRef} className="relative">
      <div className="flex items-center gap-1">
        <input
          id={id}
          type="text"
          inputMode="numeric"
          placeholder={placeholder ?? "YYYY-MM-DD"}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className={inputClassName}
        />
        <button
          type="button"
          aria-label="Ouvrir le calendrier"
          onClick={() => {
            setViewMonth(selected ?? new Date());
            setOpen((o) => !o);
          }}
          className="shrink-0 rounded-md border border-slate-300 px-2 py-2 text-slate-500 hover:bg-slate-50"
        >
          📅
        </button>
      </div>

      {open && (
        <div className="absolute z-20 mt-1 w-64 rounded-md border border-slate-200 bg-white p-3 shadow-lg">
          <div className="mb-2 flex items-center justify-between">
            <button
              type="button"
              aria-label="Mois precedent"
              onClick={() => setViewMonth(new Date(year, month - 1, 1))}
              className="rounded px-2 py-1 text-slate-500 hover:bg-slate-100"
            >
              ‹
            </button>
            <span className="text-sm font-medium text-slate-700">
              {MONTH_NAMES[month]} {year}
            </span>
            <button
              type="button"
              aria-label="Mois suivant"
              onClick={() => setViewMonth(new Date(year, month + 1, 1))}
              className="rounded px-2 py-1 text-slate-500 hover:bg-slate-100"
            >
              ›
            </button>
          </div>
          <div className="grid grid-cols-7 gap-1 text-center text-xs text-slate-400">
            {WEEKDAYS.map((w) => (
              <span key={w}>{w}</span>
            ))}
          </div>
          <div className="mt-1 grid grid-cols-7 gap-1">
            {cells.map((day, idx) => {
              if (day === null) return <span key={`blank-${idx}`} />;
              const isSelected =
                selected &&
                selected.getFullYear() === year &&
                selected.getMonth() === month &&
                selected.getDate() === day;
              return (
                <button
                  type="button"
                  key={day}
                  onClick={() => pickDay(day)}
                  className={clsx(
                    "rounded py-1 text-xs hover:bg-blue-100",
                    isSelected ? "bg-blue-600 text-white hover:bg-blue-600" : "text-slate-700"
                  )}
                >
                  {day}
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
