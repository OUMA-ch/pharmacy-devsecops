import { apiFetch } from "./client";
import type { ChartPointDTO, ReportQuery } from "../types/api";

export function getBarReport(query: ReportQuery): Promise<ChartPointDTO[]> {
  const params = new URLSearchParams({ type: query.type });
  if (query.metric) params.set("metric", query.metric);
  if (query.from) params.set("from", query.from);
  if (query.to) params.set("to", query.to);
  if (query.top !== undefined) params.set("top", String(query.top));
  if (query.seuil !== undefined) params.set("seuil", String(query.seuil));
  return apiFetch<ChartPointDTO[]>(`/reports/bar?${params.toString()}`);
}
