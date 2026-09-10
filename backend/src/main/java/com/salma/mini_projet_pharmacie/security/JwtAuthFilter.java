package com.salma.mini_projet_pharmacie.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lit le JWT de session (cookie "access_token", ou header Authorization en
 * repli pour les clients non-navigateur / tests) et peuple le
 * SecurityContext avec le role qu'il contient. Un token absent, invalide ou
 * expire laisse simplement la requete anonyme : c'est authorizeHttpRequests
 * (SecurityConfig) qui decide ensuite si la route l'exige.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                     FilterChain chain) throws ServletException, IOException {
        String token = extractToken(req);
        if (token != null && jwtService.isValid(token)) {
            Claims claims = jwtService.parse(token);
            String role = claims.get("role", String.class);
            Integer uid = claims.get("uid", Integer.class);
            var principal = new AuthenticatedUser(uid, claims.getSubject(), role);
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(req, res);
    }

    private String extractToken(HttpServletRequest req) {
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("access_token".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
