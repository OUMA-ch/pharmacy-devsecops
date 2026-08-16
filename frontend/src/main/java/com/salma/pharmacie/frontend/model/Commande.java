package com.salma.pharmacie.frontend.model;

import java.util.ArrayList;
import java.util.List;

public class Commande {

    // DTO: idCommande
    private Integer idCommande;

    // DTO: dateCommande (backend LocalDate -> json "yyyy-MM-dd")
    private String dateCommande;

    private String statut;
    private Integer fournisseurId;

    private List<LigneCommande> lignes = new ArrayList<>();

    public Integer getIdCommande() { return idCommande; }
    public void setIdCommande(Integer idCommande) { this.idCommande = idCommande; }

    public String getDateCommande() { return dateCommande; }
    public void setDateCommande(String dateCommande) { this.dateCommande = dateCommande; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Integer getFournisseurId() { return fournisseurId; }
    public void setFournisseurId(Integer fournisseurId) { this.fournisseurId = fournisseurId; }

    public List<LigneCommande> getLignes() { return lignes; }
    public void setLignes(List<LigneCommande> lignes) { this.lignes = (lignes != null) ? lignes : new ArrayList<>(); }
}
