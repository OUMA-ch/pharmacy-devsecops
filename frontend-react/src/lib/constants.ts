import type { Role, ReportMetric, ReportType, StatutCommande } from "../types/api";

export const ROLE_ESPACE_LABEL: Record<Role, string> = {
  CLIENT: "Espace Client",
  PHARMACIEN: "Espace Pharmacien",
  RESPONSABLE: "Espace Responsable"
};

export const ROLE_DISPLAY_LABEL: Record<Role, string> = {
  CLIENT: "Client",
  PHARMACIEN: "Pharmacien",
  RESPONSABLE: "Responsable"
};

export const DASHBOARD_WELCOME_TEXT: Record<"PHARMACIEN" | "RESPONSABLE", string> = {
  PHARMACIEN:
    "Gérez les produits, enregistrez les ventes, suivez les commandes et collaborez avec les fournisseurs.",
  RESPONSABLE:
    "Gérez les produits, ventes, commandes, fournisseurs, et générez des rapports en toute simplicité."
};

export interface NavItem {
  to: string;
  label: string;
  /** Style "responsable" = fond clair, cf. section 4 du cahier des charges. */
  variant?: "default" | "light";
}

export const PHARMACIEN_NAV_ITEMS: NavItem[] = [
  { to: "/produits", label: "Produits" },
  { to: "/ventes", label: "Ventes" },
  { to: "/commandes", label: "Commandes" },
  { to: "/fournisseurs", label: "Fournisseurs" }
];

export const RESPONSABLE_ONLY_NAV_ITEMS: NavItem[] = [
  { to: "/pharmaciens", label: "Gestion Pharmaciens", variant: "light" },
  { to: "/rapports", label: "Rapports", variant: "light" }
];

/**
 * CONVENTION FRONTEND (voir README) : Commande.statut est un String libre cote
 * backend, sans enum. On restreint volontairement l'UI a ces 4 valeurs.
 */
export const STATUTS_COMMANDE: StatutCommande[] = ["EN_ATTENTE", "VALIDEE", "LIVREE", "ANNULEE"];

export const STATUT_BADGE_TONE: Record<StatutCommande, "orange" | "blue" | "green" | "red"> = {
  EN_ATTENTE: "orange",
  VALIDEE: "blue",
  LIVREE: "green",
  ANNULEE: "red"
};

export const REPORT_TYPES: { value: ReportType; label: string }[] = [
  { value: "VENTES_PRODUIT", label: "Ventes par produit" },
  { value: "STOCK_PRODUIT", label: "Stock par produit" },
  { value: "STOCK_FAIBLE", label: "Stock faible" }
];

export const REPORT_METRICS: { value: ReportMetric; label: string }[] = [
  { value: "QTE", label: "Quantité" },
  { value: "CA", label: "Chiffre d'affaires" }
];

export const EMPTY_TABLE_MESSAGE = "Aucune donnée";
