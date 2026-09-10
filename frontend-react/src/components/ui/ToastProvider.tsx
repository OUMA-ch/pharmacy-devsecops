import { createContext, useCallback, useContext, useState, type ReactNode } from "react";
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

/**
 * Banniere de notification non bloquante (remplace les alert() JS pour les erreurs
 * reseau et les confirmations de succes transverses).
 */
export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const dismiss = useCallback((id: number) => {
    setToasts((current) => current.filter((t) => t.id !== id));
  }, []);

  const push = useCallback(
    (message: string, tone: Toast["tone"]) => {
      const id = nextId++;
      setToasts((current) => [...current, { id, message, tone }]);
      window.setTimeout(() => dismiss(id), 5000);
    },
    [dismiss]
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
            className={clsx(
              "min-w-[260px] max-w-sm rounded-md px-4 py-3 text-sm shadow-lg",
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
