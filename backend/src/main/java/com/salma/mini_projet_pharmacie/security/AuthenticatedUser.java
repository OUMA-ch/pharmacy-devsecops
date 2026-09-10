package com.salma.mini_projet_pharmacie.security;

/**
 * Principal Spring Security peuple par JwtAuthFilter a partir des claims du
 * JWT. Expose "uid" (l'id du User connecte) pour permettre des verifications
 * d'appartenance (IDOR) via @PreAuthorize, ex:
 * "hasAnyRole('PHARMACIEN','RESPONSABLE') or #id == principal.uid()".
 */
public record AuthenticatedUser(Integer uid, String email, String role) {
}
