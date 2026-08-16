package com.salma.mini_projet_pharmacie.dto;

public class LigneCommandeDTO {

    private Integer produitId;
    private Integer quantiteDemande;

    public Integer getProduitId() {
        return produitId;
    }

    public void setProduitId(Integer produitId) {
        this.produitId = produitId;
    }

    public Integer getQuantiteDemande() {
        return quantiteDemande;
    }

    public void setQuantiteDemande(Integer quantiteDemande) {
        this.quantiteDemande = quantiteDemande;
    }
}
