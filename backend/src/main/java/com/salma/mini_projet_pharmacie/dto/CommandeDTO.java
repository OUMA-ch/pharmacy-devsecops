package com.salma.mini_projet_pharmacie.dto;

import java.time.LocalDate;
import java.util.List;

public class CommandeDTO {

    private Integer idCommande;          // correspond à numCmd
    private LocalDate dateCommande;      // ✅ ajouté
    private String statut;
    private Integer fournisseurId;

    private List<LigneCommandeDTO> lignes;

    public Integer getIdCommande() {
        return idCommande;
    }

    public void setIdCommande(Integer idCommande) {
        this.idCommande = idCommande;
    }

    public LocalDate getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Integer getFournisseurId() {
        return fournisseurId;
    }

    public void setFournisseurId(Integer fournisseurId) {
        this.fournisseurId = fournisseurId;
    }

    public List<LigneCommandeDTO> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommandeDTO> lignes) {
        this.lignes = lignes;
    }
}
