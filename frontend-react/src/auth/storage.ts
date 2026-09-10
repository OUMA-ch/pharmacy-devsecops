import type { UserResponseDTO } from "../types/api";

const STORAGE_KEY = "pharmahoss.user";

/**
 * COMPROMIS DE SECURITE DOCUMENTE (voir README-SECURITY.md) :
 * Le backend actuel (AuthController/AuthService) ne delivre aucun token/JWT — il
 * renvoie uniquement { id, nom, email, role } apres verification des identifiants.
 * Il n'y a donc pas de session cote serveur, pas de cookie HttpOnly possible sans
 * modifier le backend (hors perimetre de cette mission). On stocke ce resultat de
 * connexion dans sessionStorage plutot que localStorage : la session ne survit pas
 * a la fermeture de l'onglet, ce qui limite (sans l'annuler) le risque de vol par
 * XSS persistant. Le controle de role applique ci-dessous (ProtectedRoute) est une
 * convention d'affichage frontend uniquement : le backend n'impose aucune
 * autorisation par role sur ses endpoints (SecurityConfig.java autorise tout).
 */
export function readStoredUser(): UserResponseDTO | null {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    return JSON.parse(raw) as UserResponseDTO;
  } catch {
    return null;
  }
}

export function writeStoredUser(user: UserResponseDTO): void {
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(user));
  } catch {
    // sessionStorage indisponible (mode prive strict) : la session reste en memoire
    // pour la duree de vie du contexte React, sans persistance au rechargement.
  }
}

export function clearStoredUser(): void {
  try {
    sessionStorage.removeItem(STORAGE_KEY);
  } catch {
    // no-op
  }
}
