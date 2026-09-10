import type { UserResponseDTO } from "../types/api";

const STORAGE_KEY = "pharmahoss.user";

/**
 * Le JWT de session vit desormais dans un cookie HttpOnly/Secure/SameSite=Strict
 * pose par le backend (AuthController.login) — il n'est jamais lisible ni
 * manipulable en JS, donc jamais stocke ici. Ce qui suit dans sessionStorage
 * n'est QUE la reponse d'affichage { id, nom, email, role } : des donnees non
 * sensibles qui evitent un flash "non connecte" au rechargement de page, avant
 * meme le premier appel API. sessionStorage (plutot que localStorage) limite la
 * duree de vie de cet affichage a l'onglet ouvert. Voir README-SECURITY.md.
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
