package com.salma.mini_projet_pharmacie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Limitation des echecs de POST /auth/login (par email ET par IP) et absence
 * d'enumeration d'utilisateurs. Chaque test utilise ses propres emails et IP
 * pour que les compteurs (en memoire, partages par le contexte Spring) ne
 * s'influencent pas.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LoginRateLimitTest {

    private static final String MOT_DE_PASSE = "secret123";
    private static final AtomicInteger IP_SUIVANTE = new AtomicInteger(1);

    @Autowired
    private MockMvc mockMvc;

    private static String emailUnique() {
        return "login" + System.nanoTime() + "@test.local";
    }

    /** IP de la plage de documentation 203.0.113.0/24, differente pour chaque appel. */
    private static String ipUnique() {
        return "203.0.113." + IP_SUIVANTE.getAndIncrement();
    }

    private void creerCompte(String email) throws Exception {
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomUser\":\"Test\",\"email\":\"" + email
                                + "\",\"password\":\"" + MOT_DE_PASSE + "\"}"))
                .andExpect(status().isOk());
    }

    private ResultActions login(String email, String password, String ip) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    @Test
    void sixiemeEchecSurLeMemeEmailRenvoie429AvecRetryAfter() throws Exception {
        String email = emailUnique();
        creerCompte(email);

        // IP differente a chaque essai : seul le compteur par email peut bloquer.
        for (int i = 0; i < 5; i++) {
            login(email, "mauvais", ipUnique()).andExpect(status().isUnauthorized());
        }

        // Email en majuscules : meme compteur (normalisation).
        login(email.toUpperCase(), "mauvais", ipUnique())
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.error").value("Trop de tentatives de connexion. Réessayez dans une minute."));

        // Meme le bon mot de passe est refuse pendant le blocage.
        login(email, MOT_DE_PASSE, ipUnique()).andExpect(status().isTooManyRequests());
    }

    @Test
    void sixiemeEchecDepuisLaMemeIpRenvoie429() throws Exception {
        String ip = ipUnique();

        // Emails inconnus tous differents : seul le compteur par IP peut bloquer,
        // et les echecs sur email inconnu sont bien comptes.
        for (int i = 0; i < 5; i++) {
            login(emailUnique(), "mauvais", ip).andExpect(status().isUnauthorized());
        }

        login(emailUnique(), "mauvais", ip)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"));
    }

    @Test
    void emailInconnuEtMauvaisMotDePasseDonnentLaMemeReponse401() throws Exception {
        String email = emailUnique();
        creerCompte(email);

        String mauvaisMotDePasse = login(email, "mauvais", ipUnique())
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        String emailInconnu = login(emailUnique(), "mauvais", ipUnique())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Email ou mot de passe incorrect."))
                .andReturn().getResponse().getContentAsString();

        assertEquals(mauvaisMotDePasse, emailInconnu);
    }

    @Test
    void loginReussiRemetLeCompteurDeLEmailAZero() throws Exception {
        String email = emailUnique();
        creerCompte(email);

        for (int i = 0; i < 4; i++) {
            login(email, "mauvais", ipUnique()).andExpect(status().isUnauthorized());
        }
        login(email, MOT_DE_PASSE, ipUnique()).andExpect(status().isOk());

        // Sans remise a zero, le compteur atteindrait 5 au 1er echec ci-dessous et
        // le 2e essai recevrait 429. Avec remise a zero : 5 nouveaux echecs en 401.
        for (int i = 0; i < 5; i++) {
            login(email, "mauvais", ipUnique()).andExpect(status().isUnauthorized());
        }
        login(email, "mauvais", ipUnique()).andExpect(status().isTooManyRequests());
    }
}
