package com.salma.mini_projet_pharmacie.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Corps de POST /users/register. Volontairement sans id ni role : le role est
 * fixe cote serveur (CLIENT) et l'id est toujours genere a l'insertion, ce qui
 * empeche d'ecraser un compte existant en envoyant son id.
 */
@Data
public class RegisterClientDTO {

    @NotBlank(message = "Le nom est obligatoire.")
    private String nomUser;

    @NotBlank(message = "L'email est obligatoire.")
    @Email(message = "Email invalide.")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères.")
    private String password;

    private String tele;
}