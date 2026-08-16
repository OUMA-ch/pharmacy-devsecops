package com.salma.mini_projet_pharmacie.dto;

public class PharmacienDTO {
    private Integer idUser;
    private String nomUser;
    private String email;
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
    public void setPassword(String password) { this.password = password; }

    public String getTele() { return tele; }
    public void setTele(String tele) { this.tele = tele; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
