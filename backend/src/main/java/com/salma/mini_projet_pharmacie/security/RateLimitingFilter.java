package com.salma.mini_projet_pharmacie.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protection brute-force sur POST /auth/login : 5 tentatives par minute et
 * par IP, au-dela renvoie 429. Stockage des compteurs en memoire
 * (ConcurrentHashMap, un bucket par IP, jamais purge) : suffisant pour une
 * seule instance backend telle que deployee ici ; une instance multi-noeuds
 * ou une tres forte volumetrie d'IP distinctes voudrait un stockage partage
 * avec expiration (Redis, Caffeine) — voir README-SECURITY.md.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String LIMITED_PATH = "/auth/login";
    private static final int MAX_ATTEMPTS_PER_MINUTE = 5;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        if (!"POST".equalsIgnoreCase(req.getMethod()) || !LIMITED_PATH.equals(req.getRequestURI())) {
            chain.doFilter(req, res);
            return;
        }

        Bucket bucket = buckets.computeIfAbsent(clientIp(req), ip -> newBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(req, res);
        } else {
            res.setStatus(429);
            res.setHeader("Retry-After", "60");
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write("{\"error\":\"Trop de tentatives de connexion. Reessayez dans une minute.\"}");
        }
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(MAX_ATTEMPTS_PER_MINUTE,
                Refill.intervally(MAX_ATTEMPTS_PER_MINUTE, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    // req.getRemoteAddr() suffit pour cette architecture (backend expose directement,
    // sans reverse-proxy dans le chemin). Derriere un proxy/load-balancer, il faudrait
    // lire X-Forwarded-For (en ne faisant confiance qu'a un proxy de confiance connu,
    // jamais a l'en-tete brut d'un client, sinon la limite devient triviale a contourner).
    private String clientIp(HttpServletRequest req) {
        return req.getRemoteAddr();
    }
}
