import { z } from "zod";

export const loginSchema = z.object({
  email: z.string().min(1, "L'email est requis.").email("Email invalide."),
  password: z.string().min(1, "Le mot de passe est requis.")
});
export type LoginFormValues = z.infer<typeof loginSchema>;

/** Telephone marocain plausible : 10 chiffres commencant par 0 (ex: 0612345678). */
const phoneRegex = /^0\d{9}$/;

export const registerSchema = z.object({
  nomUser: z.string().min(1, "Le nom complet est requis."),
  email: z.string().min(1, "L'email est requis.").email("Email invalide."),
  tele: z.string().regex(phoneRegex, "Numero invalide (10 chiffres, ex: 0612345678)."),
  password: z.string().min(6, "6 caracteres minimum.")
});
export type RegisterFormValues = z.infer<typeof registerSchema>;

export const produitSchema = z.object({
  nomCommercial: z.string().min(1, "Le nom est requis."),
  composition: z.string().optional(),
  formPharmaceutique: z.string().optional(),
  dosage: z.string().optional(),
  prixP: z.coerce.number().min(0, "Le prix doit etre positif ou nul."),
  quantiteStock: z.coerce
    .number()
    .int("Le stock doit etre un nombre entier.")
    .min(0, "Le stock doit etre positif ou nul."),
  datePeremption: z
    .string()
    .min(1, "La date de peremption est requise.")
    .refine((v) => !Number.isNaN(new Date(v).getTime()), "Date invalide.")
});
export type ProduitFormValues = z.infer<typeof produitSchema>;

export const venteSchema = z.object({
  clientId: z.coerce.number().int().positive("ID client requis."),
  produitId: z.coerce.number().int().positive("ID produit requis."),
  quantite: z.coerce.number().int().positive("Quantite requise."),
  avecOrdonnance: z.boolean()
});
export type VenteFormValues = z.infer<typeof venteSchema>;

export const ordonnanceSchema = z.object({
  nomMedecin: z.string().min(1, "Le medecin est requis."),
  dateEmission: z
    .string()
    .min(1, "La date d'emission est requise.")
    .refine((v) => !Number.isNaN(new Date(v).getTime()), "Date invalide."),
  description: z.string().optional()
});
export type OrdonnanceFormValues = z.infer<typeof ordonnanceSchema>;

export const fournisseurSchema = z.object({
  nomFournisseur: z.string().min(1, "Le nom est requis."),
  tel: z.string().optional()
});
export type FournisseurFormValues = z.infer<typeof fournisseurSchema>;

export const fournitureSchema = z.object({
  produitId: z.coerce.number().int().positive("ID produit requis."),
  prixAchat: z.coerce.number().min(0, "Le prix doit etre positif ou nul.")
});
export type FournitureFormValues = z.infer<typeof fournitureSchema>;

export const pharmacienCreateSchema = z.object({
  nomUser: z.string().min(1, "Le nom est requis."),
  email: z.string().min(1, "L'email est requis.").email("Email invalide."),
  password: z.string().min(6, "6 caracteres minimum."),
  tele: z.string().optional()
});
export type PharmacienFormValues = z.infer<typeof pharmacienCreateSchema>;

export const commandeSchema = z.object({
  fournisseurId: z.coerce.number().int().positive("ID fournisseur requis."),
  dateCommande: z
    .string()
    .min(1, "La date est requise.")
    .refine((v) => !Number.isNaN(new Date(v).getTime()), "Date invalide."),
  statut: z.enum(["EN_ATTENTE", "VALIDEE", "LIVREE", "ANNULEE"])
});
export type CommandeFormValues = z.infer<typeof commandeSchema>;
