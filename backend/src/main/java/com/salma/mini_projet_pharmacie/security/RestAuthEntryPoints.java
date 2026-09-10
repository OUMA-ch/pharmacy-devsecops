package com.salma.mini_projet_pharmacie.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * Reponses JSON coherentes avec le reste de l'API (voir GlobalExceptionHandler)
 * pour les echecs d'authentification (401 : token absent/invalide/expire) et
 * d'autorisation (403 : token valide mais role insuffisant) — jamais la page
 * d'erreur HTML par defaut de Spring Security, jamais un 500.
 */
public class RestAuthEntryPoints {

    public static AuthenticationEntryPoint unauthorized() {
        return (HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) ->
                write(res, HttpServletResponse.SC_UNAUTHORIZED, "Authentification requise.");
    }

    public static AccessDeniedHandler forbidden() {
        return (HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) ->
                write(res, HttpServletResponse.SC_FORBIDDEN, "Acces refuse pour ce role.");
    }

    private static void write(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
        res.getWriter().write("{\"error\":\"" + escaped + "\"}");
    }
}
