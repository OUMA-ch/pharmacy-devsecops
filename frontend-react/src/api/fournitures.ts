import { apiFetch } from "./client";
import type { FournitureDTO } from "../types/api";

export function listFournituresByFournisseur(idFournisseur: number): Promise<FournitureDTO[]> {
  return apiFetch<FournitureDTO[]>(`/fournitures/fournisseur/${idFournisseur}`);
}

export function ajouterFourniture(
  idProduit: number,
  idFournisseur: number,
  prixAchat: number
): Promise<FournitureDTO> {
  const params = new URLSearchParams({ prixAchat: String(prixAchat) });
  return apiFetch<FournitureDTO>(
    `/fournitures/${idProduit}/${idFournisseur}?${params.toString()}`,
    {
      method: "POST"
    }
  );
}

export function modifierPrixFourniture(
  idProduit: number,
  idFournisseur: number,
  prixAchat: number
): Promise<FournitureDTO> {
  const params = new URLSearchParams({ prixAchat: String(prixAchat) });
  return apiFetch<FournitureDTO>(
    `/fournitures/${idProduit}/${idFournisseur}?${params.toString()}`,
    {
      method: "PUT"
    }
  );
}

export function supprimerFourniture(idProduit: number, idFournisseur: number): Promise<void> {
  return apiFetch<void>(`/fournitures/${idProduit}/${idFournisseur}`, { method: "DELETE" });
}
