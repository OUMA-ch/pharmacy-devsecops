import { apiFetch } from "./client";
import type { Pharmacien, PharmacienCreateInput, PharmacienUpdateInput } from "../types/api";

export function listPharmaciens(): Promise<Pharmacien[]> {
  return apiFetch<Pharmacien[]>("/pharmaciens");
}

export function createPharmacien(input: PharmacienCreateInput): Promise<Pharmacien> {
  return apiFetch<Pharmacien>("/pharmaciens", { method: "POST", body: input });
}

export function updatePharmacien(id: number, input: PharmacienUpdateInput): Promise<Pharmacien> {
  return apiFetch<Pharmacien>(`/pharmaciens/${id}`, { method: "PUT", body: input });
}

export function deletePharmacien(id: number): Promise<void> {
  return apiFetch<void>(`/pharmaciens/${id}`, { method: "DELETE" });
}
