import { apiFetch } from "./client";
import type { CommandeDTO, CommandeCreateInput } from "../types/api";

export function listCommandes(): Promise<CommandeDTO[]> {
  return apiFetch<CommandeDTO[]>("/commandes");
}

export function createCommande(input: CommandeCreateInput): Promise<CommandeDTO> {
  return apiFetch<CommandeDTO>("/commandes", { method: "POST", body: input });
}

export function changerStatutCommande(numCmd: number, statut: string): Promise<CommandeDTO> {
  return apiFetch<CommandeDTO>(`/commandes/${numCmd}/statut`, { method: "PUT", body: { statut } });
}

export function deleteCommande(numCmd: number): Promise<void> {
  return apiFetch<void>(`/commandes/${numCmd}`, { method: "DELETE" });
}
