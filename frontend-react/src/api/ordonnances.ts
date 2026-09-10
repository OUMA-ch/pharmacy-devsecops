import { apiFetch } from "./client";
import type { OrdonnanceDTO, OrdonnanceCreateInput } from "../types/api";

export function listOrdonnancesByClient(clientId: number): Promise<OrdonnanceDTO[]> {
  return apiFetch<OrdonnanceDTO[]>(`/ordonnances/client/${clientId}`);
}

export function createOrdonnance(input: OrdonnanceCreateInput): Promise<OrdonnanceDTO> {
  return apiFetch<OrdonnanceDTO>("/ordonnances", { method: "POST", body: input });
}
