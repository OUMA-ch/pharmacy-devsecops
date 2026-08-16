package com.salma.mini_projet_pharmacie.dto;

import java.time.LocalDate;

public class OrdonnanceDTO {

    private Integer idOrdonnance;
    private LocalDate dateEmission;
    private String nomMedecin;
    private String description;
    private Integer clientId;

    public Integer getIdOrdonnance() { return idOrdonnance; }
    public void setIdOrdonnance(Integer idOrdonnance) { this.idOrdonnance = idOrdonnance; }

    public LocalDate getDateEmission() { return dateEmission; }
    public void setDateEmission(LocalDate dateEmission) { this.dateEmission = dateEmission; }

    public String getNomMedecin() { return nomMedecin; }
    public void setNomMedecin(String nomMedecin) { this.nomMedecin = nomMedecin; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }
}
