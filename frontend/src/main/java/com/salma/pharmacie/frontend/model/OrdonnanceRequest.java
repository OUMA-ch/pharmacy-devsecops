package com.salma.pharmacie.frontend.model;

public class OrdonnanceRequest {
    private String nomMedecin;
    private String description;
    private Integer clientId;
    private String dateEmission; // "yyyy-MM-dd"

    public String getNomMedecin() { return nomMedecin; }
    public void setNomMedecin(String nomMedecin) { this.nomMedecin = nomMedecin; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public String getDateEmission() { return dateEmission; }
    public void setDateEmission(String dateEmission) { this.dateEmission = dateEmission; }
}
