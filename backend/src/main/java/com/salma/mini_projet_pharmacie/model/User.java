package com.salma.mini_projet_pharmacie.model;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "`user`") // user est un mot réservé en SQL
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUser")
    private Integer idUser;

    @Column(name = "nomUser", nullable = false)
    private String nomUser;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "tele")
    private String tele;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    // ===== Constructeur sans arguments (obligatoire pour JPA) =====
    public User() {}

    // ===== Getters/Setters "officiels" (à utiliser partout dans le projet) =====

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public String getNomUser() {
        return nomUser;
    }

    public void setNomUser(String nomUser) {
        this.nomUser = nomUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTele() {
        return tele;
    }

    public void setTele(String tele) {
        this.tele = tele;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // ===== (Optionnel) Méthodes de compatibilité si tu avais déjà du code en getId/getNom/getTelephone =====
    // Tu peux supprimer ces méthodes si tu veux forcer tout le projet à utiliser IdUser/NomUser/Tele

    public Integer getId() { return idUser; }
    public void setId(Integer id) { this.idUser = id; }

    public String getNom() { return nomUser; }
    public void setNom(String nom) { this.nomUser = nom; }

    public String getTelephone() { return tele; }
    public void setTelephone(String telephone) { this.tele = telephone; }
}
