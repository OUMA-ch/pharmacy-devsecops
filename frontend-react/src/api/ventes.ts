import { apiFetch } from "./client";
import type { VenteDTO, VenteCreateInput } from "../types/api";

export function listVentes(): Promise<VenteDTO[]> {
  return apiFetch<VenteDTO[]>("/ventes");
}

export function listVentesByClient(clientId: number): Promise<VenteDTO[]> {
  return apiFetch<VenteDTO[]>(`/ventes/client/${clientId}`);
}

export function createVente(input: VenteCreateInput): Promise<VenteDTO> {
  return apiFetch<VenteDTO>("/ventes", { method: "POST", body: input });
}

export function updateVente(id: number, input: VenteCreateInput): Promise<VenteDTO> {
  return apiFetch<VenteDTO>(`/ventes/${id}`, { method: "PUT", body: input });
}

export function deleteVente(id: number): Promise<void> {
  return apiFetch<void>(`/ventes/${id}`, { method: "DELETE" });
}
