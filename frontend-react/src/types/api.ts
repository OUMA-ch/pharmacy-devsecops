/**
 * Types miroir des DTOs/entites reellement exposes par le backend Spring Boot
 * (backend/src/main/java/.../dto et .../model). Aucun champ invente : chaque
 * interface correspond exactement a ce que l'API renvoie ou attend.
 */

export type Role = "CLIENT" | "PHARMACIEN" | "RESPONSABLE";

export interface UserResponseDTO {
  id: number;
  nom: string;
  email: string;
  role: Role;
}

export interface Produit {
  idProduit: number;
  nomCommercial: string;
  composition: string | null;
  prixP: number;
  formPharmaceutique: string | null;
  dosage: string | null;
  datePeremption: string | null; // format ISO YYYY-MM-DD
  quantiteStock: number;
}

export type ProduitInput = Omit<Produit, "idProduit">;

export interface Fournisseur {
  idFournisseur: number;
  nomFournisseur: string;
  tel: string | null;
}

export type FournisseurInput = Omit<Fournisseur, "idFournisseur">;

export interface FournitureDTO {
  idProduit: number;
  idFournisseur: number;
  prixAchat: number;
  nomProduit: string | null;
}

/**
 * Les 4 valeurs ci-dessous sont une CONVENTION FRONTEND, pas un enum backend :
 * CommandeService.java stocke `statut` en String libre, sans validation serveur
 * (voir README pour le detail). On restreint volontairement l'UI a ces valeurs.
 */
export type StatutCommande = "EN_ATTENTE" | "VALIDEE" | "LIVREE" | "ANNULEE";

export interface LigneCommandeDTO {
  produitId: number;
  quantiteDemande: number;
}

export interface CommandeDTO {
  idCommande: number;
  dateCommande: string | null; // YYYY-MM-DD
  statut: string;
  fournisseurId: number | null;
  lignes: LigneCommandeDTO[];
}

export interface CommandeCreateInput {
  fournisseurId: number;
  dateCommande: string;
  statut: StatutCommande;
  lignes: LigneCommandeDTO[];
}

export interface VenteDTO {
  idVente: number;
  quantite: number;
  dateVente: string | null;
  clientId: number;
  produitId: number;
  ordonnanceId: number | null;
  prixTotal: number;
  ordonnanceNomMedecin: string | null;
  ordonnanceDateEmission: string | null;
  ordonnanceDescription: string | null;
}

export interface VenteCreateInput {
  produitId: number;
  clientId: number;
  quantite: number;
  dateVente: string;
  ordonnanceId?: number | null;
}

export interface OrdonnanceDTO {
  idOrdonnance: number;
  dateEmission: string;
  nomMedecin: string;
  description: string | null;
  clientId: number;
}

export interface OrdonnanceCreateInput {
  clientId: number;
  dateEmission: string;
  nomMedecin: string;
  description?: string;
}

export interface Notification {
  id: number;
  titre: string;
  message: string;
  dateEnvoi: string; // LocalDateTime ISO
  lu: boolean;
}

export interface Pharmacien {
  idUser: number;
  nomUser: string;
  email: string;
  tele: string | null;
  role: "PHARMACIEN";
}

export interface PharmacienCreateInput {
  nomUser: string;
  email: string;
  password: string;
  tele?: string;
  role: "PHARMACIEN";
}

export interface PharmacienUpdateInput {
  nomUser?: string;
  email?: string;
  password?: string;
  tele?: string;
}

export interface ChartPointDTO {
  label: string;
  value: number;
}

export type ReportType = "VENTES_PRODUIT" | "STOCK_PRODUIT" | "STOCK_FAIBLE";
export type ReportMetric = "QTE" | "CA";

export interface ReportQuery {
  type: ReportType;
  metric?: ReportMetric;
  from?: string;
  to?: string;
  top?: number;
  seuil?: number;
}

export interface RegisterClientInput {
  nomUser: string;
  email: string;
  password: string;
  tele?: string;
}

export interface LoginInput {
  email: string;
  password: string;
}
