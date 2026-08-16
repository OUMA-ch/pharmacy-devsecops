package com.salma.pharmacie.frontend.model;

public class Pharmacien {
    private Integer idUser;
    private String nomUser;
    private String email;
    private String tele;
    private String role;

    public Integer getIdUser() { return idUser; }
    public void setIdUser(Integer idUser) { this.idUser = idUser; }

    public String getNomUser() { return nomUser; }
    public void setNomUser(String nomUser) { this.nomUser = nomUser; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTele() { return tele; }
    public void setTele(String tele) { this.tele = tele; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
