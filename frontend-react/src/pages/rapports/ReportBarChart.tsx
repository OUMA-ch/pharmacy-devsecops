import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from "recharts";
import type { ChartPointDTO } from "../../types/api";

interface ReportBarChartProps {
  data: ChartPointDTO[];
  seriesLabel: string;
}

/**
 * Palette categorielle sobre et stable (pas de couleur aleatoire a chaque clic
 * "Afficher", contrairement a l'app d'origine — voir points ouverts du README).
 */
const PALETTE = [
  "#2563eb",
  "#059669",
  "#d97706",
  "#dc2626",
  "#7c3aed",
  "#0891b2",
  "#db2777",
  "#65a30d"
];

export function ReportBarChart({ data, seriesLabel }: ReportBarChartProps) {
  if (data.length === 0) {
    return <p className="py-16 text-center text-slate-400">Aucune donnée pour ces filtres.</p>;
  }

  return (
    <ResponsiveContainer width="100%" height={420}>
      <BarChart data={data} margin={{ top: 20, right: 20, bottom: 70, left: 10 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
        <XAxis
          dataKey="label"
          angle={-40}
          textAnchor="end"
          interval={0}
          height={80}
          tick={{ fontSize: 12 }}
        />
        <YAxis />
        <Tooltip />
        <Legend verticalAlign="bottom" wrapperStyle={{ paddingTop: 12 }} />
        <Bar dataKey="value" name={seriesLabel} isAnimationActive animationDuration={400}>
          {data.map((entry, index) => (
            <Cell key={entry.label} fill={PALETTE[index % PALETTE.length]} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  );
}
