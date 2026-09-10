import { apiFetch } from "./client";
import type { Fournisseur, FournisseurInput } from "../types/api";

export function listFournisseurs(): Promise<Fournisseur[]> {
  return apiFetch<Fournisseur[]>("/fournisseurs");
}

export function createFournisseur(input: FournisseurInput): Promise<Fournisseur> {
  return apiFetch<Fournisseur>("/fournisseurs", { method: "POST", body: input });
}

export function updateFournisseur(id: number, input: FournisseurInput): Promise<Fournisseur> {
  return apiFetch<Fournisseur>(`/fournisseurs/${id}`, { method: "PUT", body: input });
}

export function deleteFournisseur(id: number): Promise<void> {
  return apiFetch<void>(`/fournisseurs/${id}`, { method: "DELETE" });
}
