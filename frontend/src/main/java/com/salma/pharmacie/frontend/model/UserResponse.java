package com.salma.pharmacie.frontend.model;

import lombok.Data;

/**
 * Réponse du backend pour POST /auth/login
 */
@Data


public class UserResponse {
    private Integer id;
    private String nom;
    private String email;
    private String role;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
