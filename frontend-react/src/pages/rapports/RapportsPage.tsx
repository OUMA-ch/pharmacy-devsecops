import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import * as reportsApi from "../../api/reports";
import { PageHeader } from "../../components/layout/PageHeader";
import { Button } from "../../components/ui/Button";
import { Spinner } from "../../components/ui/Spinner";
import { DatePickerField } from "../../components/ui/DatePickerField";
import { REPORT_METRICS, REPORT_TYPES } from "../../lib/constants";
import type { ReportMetric, ReportQuery, ReportType } from "../../types/api";
import { ReportBarChart } from "./ReportBarChart";

export function RapportsPage() {
  const [type, setType] = useState<ReportType>("VENTES_PRODUIT");
  const [metric, setMetric] = useState<ReportMetric>("QTE");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [top, setTop] = useState("10");
  const [seuil, setSeuil] = useState("10");
  const [submitted, setSubmitted] = useState<ReportQuery | null>(null);

  const reportQuery = useQuery({
    queryKey: ["report", submitted],
    queryFn: () => reportsApi.getBarReport(submitted!),
    enabled: submitted !== null
  });

  function handleAfficher() {
    const query: ReportQuery = { type, top: Number(top) || 10 };
    // Le backend (ReportService.java) n'utilise "metric" que pour VENTES_PRODUIT et
    // "seuil"/"from"/"to" que pour STOCK_FAIBLE / VENTES_PRODUIT respectivement : on
    // n'envoie donc que les parametres pertinents pour le type choisi.
    if (type === "VENTES_PRODUIT") {
      query.metric = metric;
      if (from) query.from = from;
      if (to) query.to = to;
    }
    if (type === "STOCK_FAIBLE") {
      query.seuil = Number(seuil) || 10;
    }
    setSubmitted(query);
  }

  const seriesLabel = `${type}${type === "VENTES_PRODUIT" ? ` / ${metric}` : ""}`;

  return (
    <div>
      <PageHeader title="Rapports (Courbe en bâtons)" />

      <div className="mb-6 flex flex-wrap items-end gap-4 rounded-lg border border-slate-200 bg-white p-4">
        <div>
          <label htmlFor="report-type" className="mb-1 block text-xs font-medium text-slate-600">
            Type:
          </label>
          <select
            id="report-type"
            value={type}
            onChange={(e) => setType(e.target.value as ReportType)}
            className="rounded-md border border-slate-300 px-3 py-2 text-sm"
          >
            {REPORT_TYPES.map((t) => (
              <option key={t.value} value={t.value}>
                {t.value}
              </option>
            ))}
          </select>
        </div>

        {type === "VENTES_PRODUIT" && (
          <div>
            <label
              htmlFor="report-metric"
              className="mb-1 block text-xs font-medium text-slate-600"
            >
              Metric:
            </label>
            <select
              id="report-metric"
              value={metric}
              onChange={(e) => setMetric(e.target.value as ReportMetric)}
              className="rounded-md border border-slate-300 px-3 py-2 text-sm"
            >
              {REPORT_METRICS.map((m) => (
                <option key={m.value} value={m.value}>
                  {m.value}
                </option>
              ))}
            </select>
          </div>
        )}

        {type === "VENTES_PRODUIT" && (
          <>
            <div>
              <label
                htmlFor="report-from"
                className="mb-1 block text-xs font-medium text-slate-600"
              >
                De:
              </label>
              <DatePickerField id="report-from" value={from} onChange={setFrom} />
            </div>
            <div>
              <label htmlFor="report-to" className="mb-1 block text-xs font-medium text-slate-600">
                À:
              </label>
              <DatePickerField id="report-to" value={to} onChange={setTo} />
            </div>
          </>
        )}

        <div>
          <label htmlFor="report-top" className="mb-1 block text-xs font-medium text-slate-600">
            Top:
          </label>
          <input
            id="report-top"
            type="number"
            min={1}
            value={top}
            onChange={(e) => setTop(e.target.value)}
            className="w-20 rounded-md border border-slate-300 px-3 py-2 text-sm"
          />
        </div>

        {type === "STOCK_FAIBLE" && (
          <div>
            <label htmlFor="report-seuil" className="mb-1 block text-xs font-medium text-slate-600">
              Seuil:
            </label>
            <input
              id="report-seuil"
              type="number"
              min={0}
              value={seuil}
              onChange={(e) => setSeuil(e.target.value)}
              className="w-20 rounded-md border border-slate-300 px-3 py-2 text-sm"
            />
          </div>
        )}

        <Button variant="primary" onClick={handleAfficher}>
          Afficher
        </Button>
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-4">
        {submitted === null ? (
          <p className="py-16 text-center text-slate-400">
            Choisissez des filtres puis cliquez sur "Afficher".
          </p>
        ) : reportQuery.isLoading ? (
          <Spinner label="Génération du graphique..." />
        ) : (
          <ReportBarChart data={reportQuery.data ?? []} seriesLabel={seriesLabel} />
        )}
      </div>
    </div>
  );
}
