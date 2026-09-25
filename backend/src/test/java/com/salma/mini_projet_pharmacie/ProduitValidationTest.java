package com.salma.mini_projet_pharmacie;

import com.salma.mini_projet_pharmacie.repository.ProduitRepository;
import com.salma.mini_projet_pharmacie.security.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Validation serveur de POST /produits, a travers la vraie chaine de securite :
 * le JWT est envoye dans le cookie "access_token" comme le fait le navigateur.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProduitValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ProduitRepository produitRepository;

    private Cookie pharmacienCookie() {
        return new Cookie("access_token", jwtService.generateToken("test@pharmacie.local", "PHARMACIEN", 1));
    }

    @Test
    void prixNegatifRenvoie400EtNEnregistreRien() throws Exception {
        long avant = produitRepository.count();

        mockMvc.perform(post("/produits")
                        .cookie(pharmacienCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomCommercial\":\"Test\",\"prixP\":-5,\"quantiteStock\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.prixP").value("Le prix doit être positif ou nul."));

        assertEquals(avant, produitRepository.count());
    }

    @Test
    void nomVideRenvoie400EtNEnregistreRien() throws Exception {
        long avant = produitRepository.count();

        mockMvc.perform(post("/produits")
                        .cookie(pharmacienCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomCommercial\":\"  \",\"prixP\":5,\"quantiteStock\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.nomCommercial").value("Le nom du produit est obligatoire."));

        assertEquals(avant, produitRepository.count());
    }

    @Test
    void sansCookieRenvoie401() throws Exception {
        mockMvc.perform(post("/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomCommercial\":\"Test\",\"prixP\":-5,\"quantiteStock\":10}"))
                .andExpect(status().isUnauthorized());
    }
}
