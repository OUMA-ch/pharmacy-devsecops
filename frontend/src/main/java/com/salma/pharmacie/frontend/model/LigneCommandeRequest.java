package com.salma.pharmacie.frontend.model;

public class LigneCommandeRequest {
    private Integer produitId;
    private Integer quantiteDemande;

    public Integer getProduitId() { return produitId; }
    public void setProduitId(Integer produitId) { this.produitId = produitId; }

    public Integer getQuantiteDemande() { return quantiteDemande; }
    public void setQuantiteDemande(Integer quantiteDemande) { this.quantiteDemande = quantiteDemande; }
}
