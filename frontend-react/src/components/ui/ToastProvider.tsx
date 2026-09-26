import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
  type ReactNode
} from "react";
import { useLocation } from "react-router-dom";
import clsx from "clsx";

interface Toast {
  id: number;
  message: string;
  tone: "error" | "success";
}

interface ToastContextValue {
  showError: (message: string) => void;
  showSuccess: (message: string) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

let nextId = 1;

/** Nombre max de toasts affiches simultanement : au-dela, les plus anciens sont
 * retires immediatement au lieu de s'empiler sans fin (cf. bug rapporte : des
 * clics repetes sur "Supprimer" empilaient des dizaines de bandeaux identiques). */
const MAX_TOASTS = 3;
const AUTO_DISMISS_MS = 5000;

/**
 * Banniere de notification non bloquante (remplace les alert() JS pour les erreurs
 * reseau et les confirmations de succes transverses). Auto-disparition apres
 * AUTO_DISMISS_MS, fermeture manuelle au clic, et deduplication des messages
 * identiques consecutifs (rafraichit son timer au lieu d'en empiler un doublon).
 */
export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const timers = useRef(new Map<number, number>());

  const dismiss = useCallback((id: number) => {
    const timer = timers.current.get(id);
    if (timer !== undefined) {
      window.clearTimeout(timer);
      timers.current.delete(id);
    }
    setToasts((current) => current.filter((t) => t.id !== id));
  }, []);

  const dismissAll = useCallback(() => {
    for (const timer of timers.current.values()) window.clearTimeout(timer);
    timers.current.clear();
    setToasts([]);
  }, []);

  // Une notification appartient a l'action/page qui l'a declenchee : sans ce
  // nettoyage, un toast pas encore auto-disparu (ex: "Cette suppression est
  // impossible...") pouvait rester visible apres avoir navigue vers un tout
  // autre module et se confondre avec l'erreur d'une action sans rapport
  // (bug constate : ordonnance perimee sur Ventes affichant l'ancien message
  // Fournisseurs).
  const location = useLocation();
  const previousPathname = useRef(location.pathname);
  useEffect(() => {
    if (previousPathname.current === location.pathname) return;
    previousPathname.current = location.pathname;
    dismissAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.pathname]);

  const scheduleDismiss = useCallback(
    (id: number) => {
      const timer = window.setTimeout(() => dismiss(id), AUTO_DISMISS_MS);
      timers.current.set(id, timer);
    },
    [dismiss]
  );

  const push = useCallback(
    (message: string, tone: Toast["tone"]) => {
      setToasts((current) => {
        const duplicate = current.find((t) => t.tone === tone && t.message === message);
        if (duplicate) {
          const existingTimer = timers.current.get(duplicate.id);
          if (existingTimer !== undefined) window.clearTimeout(existingTimer);
          scheduleDismiss(duplicate.id);
          return current;
        }

        const id = nextId++;
        scheduleDismiss(id);
        const next = [...current, { id, message, tone }];
        if (next.length > MAX_TOASTS) {
          const overflow = next.splice(0, next.length - MAX_TOASTS);
          for (const removed of overflow) {
            const staleTimer = timers.current.get(removed.id);
            if (staleTimer !== undefined) {
              window.clearTimeout(staleTimer);
              timers.current.delete(removed.id);
            }
          }
        }
        return next;
      });
    },
    [scheduleDismiss]
  );

  const showError = useCallback((message: string) => push(message, "error"), [push]);
  const showSuccess = useCallback((message: string) => push(message, "success"), [push]);

  return (
    <ToastContext.Provider value={{ showError, showSuccess }}>
      {children}
      <div className="fixed bottom-4 right-4 z-[100] flex flex-col gap-2">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            role="alert"
            onClick={() => dismiss(toast.id)}
            className={clsx(
              "min-w-[260px] max-w-sm cursor-pointer rounded-md px-4 py-3 text-sm shadow-lg",
              toast.tone === "error" ? "bg-red-600 text-white" : "bg-emerald-600 text-white"
            )}
          >
            {toast.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error("useToast must be used within a ToastProvider");
  return ctx;
}
