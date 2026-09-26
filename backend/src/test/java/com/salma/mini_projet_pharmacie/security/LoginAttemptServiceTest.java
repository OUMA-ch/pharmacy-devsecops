package com.salma.mini_projet_pharmacie.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Fenetre glissante de 60 s de LoginAttemptService, avec une horloge controlee. */
class LoginAttemptServiceTest {

    /** Horloge dont on peut avancer l'heure. */
    private static final class HorlogeReglable extends Clock {
        private Instant maintenant = Instant.parse("2026-01-01T00:00:00Z");

        void avancer(Duration duree) {
            maintenant = maintenant.plus(duree);
        }

        @Override
        public Instant instant() {
            return maintenant;
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }

    @Test
    void lesEchecsExpirentApresLaFenetreDe60Secondes() {
        HorlogeReglable horloge = new HorlogeReglable();
        LoginAttemptService service = new LoginAttemptService(horloge);

        for (int i = 0; i < 5; i++) {
            service.recordFailure("a@test.local", "198.51.100.1");
        }
        assertTrue(service.isBlocked("a@test.local", "198.51.100.2"));
        assertTrue(service.isBlocked("autre@test.local", "198.51.100.1"));

        horloge.avancer(Duration.ofSeconds(61));

        assertFalse(service.isBlocked("a@test.local", "198.51.100.1"));
    }

    @Test
    void resetEmailNeRemetPasLeCompteurIpAZero() {
        LoginAttemptService service = new LoginAttemptService(new HorlogeReglable());

        for (int i = 0; i < 5; i++) {
            service.recordFailure("A@Test.local ", "198.51.100.1");
        }
        service.resetEmail("a@test.local");

        assertFalse(service.isBlocked("a@test.local", "198.51.100.9"));
        assertTrue(service.isBlocked("a@test.local", "198.51.100.1"));
    }
}
