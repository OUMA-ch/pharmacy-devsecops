import type { ReactNode } from "react";
import clsx from "clsx";

type BadgeTone = "green" | "gray" | "orange" | "red" | "blue";

const TONE_CLASSES: Record<BadgeTone, string> = {
  green: "bg-green-100 text-green-800",
  gray: "bg-slate-100 text-slate-600",
  orange: "bg-orange-100 text-orange-800",
  red: "bg-red-100 text-red-800",
  blue: "bg-blue-100 text-blue-800"
};

export function Badge({ tone, children }: { tone: BadgeTone; children: ReactNode }) {
  return (
    <span
      className={clsx(
        "inline-block rounded-full px-2.5 py-0.5 text-xs font-semibold",
        TONE_CLASSES[tone]
      )}
    >
      {children}
    </span>
  );
}
