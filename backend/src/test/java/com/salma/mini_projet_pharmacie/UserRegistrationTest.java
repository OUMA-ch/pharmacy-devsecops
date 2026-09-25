package com.salma.mini_projet_pharmacie;

import com.jayway.jsonpath.JsonPath;
import com.salma.mini_projet_pharmacie.model.Role;
import com.salma.mini_projet_pharmacie.model.User;
import com.salma.mini_projet_pharmacie.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * POST /users/register : le role et l'id envoyes par le client sont ignores,
 * le mot de passe n'apparait jamais dans la reponse.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserRegistrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private static String emailUnique() {
        return "client" + System.nanoTime() + "@test.local";
    }

    /** Inscrit un client, verifie 200 sans mot de passe dans la reponse, renvoie le JSON. */
    private String inscrire(String json) throws Exception {
        return mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void inscriptionAvecRoleResponsableCreeUnCompteClient() throws Exception {
        String email = emailUnique();

        String reponse = inscrire("{\"nomUser\":\"Pirate\",\"email\":\"" + email
                + "\",\"password\":\"secret123\",\"tele\":\"0612345678\",\"role\":\"RESPONSABLE\"}");

        assertEquals("CLIENT", JsonPath.read(reponse, "$.role"));
        User enBase = userRepository.findByEmail(email).orElseThrow();
        assertEquals(Role.CLIENT, enBase.getRole());
    }

    @Test
    void inscriptionAvecIdExistantNEcrasePasLeCompte() throws Exception {
        String emailVictime = emailUnique();
        int idVictime = JsonPath.read(inscrire("{\"nomUser\":\"Victime\",\"email\":\"" + emailVictime
                + "\",\"password\":\"secret123\"}"), "$.id");

        String emailPirate = emailUnique();
        int idPirate = JsonPath.read(inscrire("{\"idUser\":" + idVictime + ",\"id\":" + idVictime
                + ",\"nomUser\":\"Pirate\",\"email\":\"" + emailPirate + "\",\"password\":\"secret123\"}"), "$.id");

        assertNotEquals(idVictime, idPirate);
        assertEquals(emailVictime, userRepository.findById(idVictime).orElseThrow().getEmail());
    }

    @Test
    void emailInvalideRenvoie400() throws Exception {
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomUser\":\"Test\",\"email\":\"pas-un-email\",\"password\":\"secret123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.email").value("Email invalide."));
    }
}