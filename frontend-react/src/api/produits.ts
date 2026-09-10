import { apiFetch } from "./client";
import type { Produit, ProduitInput } from "../types/api";

export function listProduits(): Promise<Produit[]> {
  return apiFetch<Produit[]>("/produits");
}

export function listStockFaible(): Promise<Produit[]> {
  return apiFetch<Produit[]>("/produits/alerte-stock");
}

export function listPeremption(): Promise<Produit[]> {
  return apiFetch<Produit[]>("/produits/peremption");
}

export function createProduit(input: ProduitInput): Promise<Produit> {
  return apiFetch<Produit>("/produits", { method: "POST", body: input });
}

export function updateProduit(id: number, input: ProduitInput): Promise<Produit> {
  return apiFetch<Produit>(`/produits/${id}`, { method: "PUT", body: input });
}

export function deleteProduit(id: number): Promise<void> {
  return apiFetch<void>(`/produits/${id}`, { method: "DELETE" });
}
