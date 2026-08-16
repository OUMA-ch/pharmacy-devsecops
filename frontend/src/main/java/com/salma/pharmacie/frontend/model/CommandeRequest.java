package com.salma.pharmacie.frontend.model;

import java.util.ArrayList;
import java.util.List;

public class CommandeRequest {
    private Integer fournisseurId;
    private String statut;
    private List<LigneCommandeRequest> lignes = new ArrayList<>();

    public Integer getFournisseurId() { return fournisseurId; }
    public void setFournisseurId(Integer fournisseurId) { this.fournisseurId = fournisseurId; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public List<LigneCommandeRequest> getLignes() { return lignes; }
    public void setLignes(List<LigneCommandeRequest> lignes) { this.lignes = lignes; }
}
