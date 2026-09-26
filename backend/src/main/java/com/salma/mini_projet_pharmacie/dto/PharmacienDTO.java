package com.salma.mini_projet_pharmacie.dto;

import com.salma.mini_projet_pharmacie.validation.OnCreate;
import com.salma.mini_projet_pharmacie.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;

public class PharmacienDTO {
    private Integer idUser;
    private String nomUser;
    private String email;
    /** Obligatoire a la creation ; a la modification, absent ou vide = inchange. */
    @NotBlank(groups = OnCreate.class, message = "Le mot de passe est obligatoire.")
    @StrongPassword
    private String password;
    private String tele; // important
    private String role; // "PHARMACIEN"

    public Integer getIdUser() { return idUser; }
    public void setIdUser(Integer idUser) { this.idUser = idUser; }

    public String getNomUser() { return nomUser; }
    public void setNomUser(String nomUser) { this.nomUser = nomUser; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    // Vide ramene a null : en modification, "" signifie "ne pas changer" et ne doit
    // donc pas etre soumis a @StrongPassword (qui ignore null).
    public void setPassword(String password) {
        this.password = (password == null || password.isBlank()) ? null : password;
    }

    public String getTele() { return tele; }
    public void setTele(String tele) { this.tele = tele; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
