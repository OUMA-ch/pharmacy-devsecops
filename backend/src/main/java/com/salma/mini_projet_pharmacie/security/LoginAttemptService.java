package com.salma.mini_projet_pharmacie.security;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protection brute-force de POST /auth/login : compte les ECHECS de connexion
 * sur une fenetre glissante de 60 s, par email (normalise en minuscules, email
 * inconnu compris) ET par IP client. Au-dela de 5 echecs sur l'une des deux
 * cles, la tentative suivante est refusee (429, voir AuthService).
 *
 * - Par email : un attaquant qui change d'IP ne peut pas tester sans limite
 *   les mots de passe d'un meme compte.
 * - Par IP : un attaquant ne peut pas tester un mot de passe courant sur
 *   beaucoup de comptes. L'IP vient de getRemoteAddr(), qui renvoie l'IP reelle
 *   derriere le proxy Render grace a server.forward-headers-strategy=native
 *   (RemoteIpValve Tomcat, voir application.properties).
 *
 * Stockage en memoire (instance unique deployee) : les compteurs sont perdus au
 * redemarrage ; un deploiement multi-instances voudrait un stockage partage (Redis).
 */
@Component
public class LoginAttemptService {

    public static final int MAX_FAILURES = 5;
    public static final Duration WINDOW = Duration.ofSeconds(60);

    // Au-dela de ce nombre de cles, purge des fenetres expirees pour borner la memoire.
    private static final int PURGE_THRESHOLD = 10_000;

    private final Map<String, Deque<Long>> failures = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    /** Vrai si l'email OU l'IP a deja atteint MAX_FAILURES echecs dans la fenetre. */
    public boolean isBlocked(String email, String ip) {
        return recentFailures(emailKey(email)) >= MAX_FAILURES
                || recentFailures(ipKey(ip)) >= MAX_FAILURES;
    }

    public void recordFailure(String email, String ip) {
        long now = clock.millis();
        addFailure(emailKey(email), now);
        addFailure(ipKey(ip), now);
        if (failures.size() > PURGE_THRESHOLD) {
            purgeExpired(now);
        }
    }

    /**
     * Connexion reussie : remet a zero le compteur de cet email uniquement.
     * Le compteur IP n'est pas remis a zero, sinon un attaquant pourrait le vider
     * en se connectant regulierement avec son propre compte.
     */
    public void resetEmail(String email) {
        failures.remove(emailKey(email));
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String emailKey(String email) {
        return "email:" + normalizeEmail(email);
    }

    private static String ipKey(String ip) {
        return "ip:" + (ip == null ? "" : ip);
    }

    private int recentFailures(String key) {
        Deque<Long> timestamps = failures.get(key);
        if (timestamps == null) {
            return 0;
        }
        synchronized (timestamps) {
            dropExpired(timestamps, clock.millis());
            return timestamps.size();
        }
    }

    private void addFailure(String key, long now) {
        Deque<Long> timestamps = failures.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            dropExpired(timestamps, now);
            timestamps.addLast(now);
        }
    }

    private void purgeExpired(long now) {
        failures.entrySet().removeIf(entry -> {
            Deque<Long> timestamps = entry.getValue();
            synchronized (timestamps) {
                dropExpired(timestamps, now);
                return timestamps.isEmpty();
            }
        });
    }

    private static void dropExpired(Deque<Long> timestamps, long now) {
        long limit = now - WINDOW.toMillis();
        while (!timestamps.isEmpty() && timestamps.peekFirst() <= limit) {
            timestamps.pollFirst();
        }
    }
}
