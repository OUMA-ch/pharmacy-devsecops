package com.salma.mini_projet_pharmacie;

import com.jayway.jsonpath.JsonPath;
import com.salma.mini_projet_pharmacie.model.Client;
import com.salma.mini_projet_pharmacie.model.Role;
import com.salma.mini_projet_pharmacie.repository.ClientRepository;
import com.salma.mini_projet_pharmacie.repository.UserRepository;
import com.salma.mini_projet_pharmacie.security.JwtService;
import com.salma.mini_projet_pharmacie.validation.StrongPassword;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Politique de mot de passe (@StrongPassword) sur l'inscription client et la
 * creation/modification d'un pharmacien. La connexion n'y est pas soumise.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PasswordPolicyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static String emailUnique(String prefixe) {
        return prefixe + System.nanoTime() + "@test.local";
    }

    private Cookie responsableCookie() {
        return new Cookie("access_token", jwtService.generateToken("resp@pharmacie.local", "RESPONSABLE", 1));
    }

    private ResultActions inscrire(String email, String password) throws Exception {
        return mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nomUser\":\"Test\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    private ResultActions creerPharmacien(String email, String password) throws Exception {
        return mockMvc.perform(post("/pharmaciens")
                .cookie(responsableCookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nomUser\":\"Pharma\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    // --- Inscription client ---

    @ParameterizedTest
    @ValueSource(strings = {"123456789", "abcdefgh"})
    void inscriptionAvecMotDePasseFaibleRenvoie400(String password) throws Exception {
        String email = emailUnique("faible");

        inscrire(email, password)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.password").value(StrongPassword.MESSAGE));

        assertFalse(userRepository.findByEmail(email).isPresent());
    }

    @Test
    void inscriptionAvecMotDePasseConformeCreeLeCompte() throws Exception {
        String email = emailUnique("fort");

        inscrire(email, "Test1234").andExpect(status().isOk());

        assertTrue(userRepository.findByEmail(email).isPresent());
    }

    // --- Creation d'un pharmacien (RESPONSABLE) ---

    @ParameterizedTest
    @ValueSource(strings = {"123456789", "abcdefgh"})
    void creationPharmacienAvecMotDePasseFaibleRenvoie400(String password) throws Exception {
        String email = emailUnique("pharma-faible");

        creerPharmacien(email, password)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.password").value(StrongPassword.MESSAGE));

        assertFalse(userRepository.findByEmail(email).isPresent());
    }

    @Test
    void creationPharmacienSansMotDePasseRenvoie400() throws Exception {
        mockMvc.perform(post("/pharmaciens")
                        .cookie(responsableCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomUser\":\"Pharma\",\"email\":\"" + emailUnique("pharma-vide") + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.password").value("Le mot de passe est obligatoire."));
    }

    @Test
    void creationPharmacienAvecMotDePasseConformeCreeLeCompte() throws Exception {
        String email = emailUnique("pharma-fort");

        creerPharmacien(email, "Test1234").andExpect(status().isOk());

        assertEquals(Role.PHARMACIEN, userRepository.findByEmail(email).orElseThrow().getRole());
    }

    // --- Modification d'un pharmacien ---

    @Test
    void modificationPharmacienAvecMotDePasseFaibleRenvoie400EtNeChangeRien() throws Exception {
        String email = emailUnique("pharma-modif");
        int id = JsonPath.read(creerPharmacien(email, "Test1234").andReturn().getResponse().getContentAsString(),
                "$.idUser");
        String hashAvant = userRepository.findById(id).orElseThrow().getPassword();

        mockMvc.perform(put("/pharmaciens/" + id)
                        .cookie(responsableCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"123456789\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.password").value(StrongPassword.MESSAGE));

        assertEquals(hashAvant, userRepository.findById(id).orElseThrow().getPassword());
    }

    @Test
    void modificationPharmacienAvecMotDePasseVideConserveLAncien() throws Exception {
        String email = emailUnique("pharma-inchange");
        int id = JsonPath.read(creerPharmacien(email, "Test1234").andReturn().getResponse().getContentAsString(),
                "$.idUser");
        String hashAvant = userRepository.findById(id).orElseThrow().getPassword();

        mockMvc.perform(put("/pharmaciens/" + id)
                        .cookie(responsableCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomUser\":\"Renomme\",\"password\":\"\"}"))
                .andExpect(status().isOk());

        assertEquals(hashAvant, userRepository.findById(id).orElseThrow().getPassword());
    }

    // --- Connexion : comptes existants non concernes ---

    @Test
    void compteExistantAvecMotDePasseFaiblePeutToujoursSeConnecter() throws Exception {
        String email = emailUnique("ancien");
        Client ancien = new Client();
        ancien.setNomUser("Ancien");
        ancien.setEmail(email);
        ancien.setPassword(passwordEncoder.encode("123456"));
        ancien.setRole(Role.CLIENT);
        clientRepository.save(ancien);

        mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("198.51.100.10");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk());
    }
}
