package com.salma.pharmacie.frontend.model;

public class LigneCommande {

    private Integer produitId;
    private Integer quantiteDemande;

    public Integer getProduitId() { return produitId; }
    public void setProduitId(Integer produitId) { this.produitId = produitId; }

    public Integer getQuantiteDemande() { return quantiteDemande; }
    public void setQuantiteDemande(Integer quantiteDemande) { this.quantiteDemande = quantiteDemande; }

    // pour TableColumn qteCol (PropertyValueFactory("quantite"))
    public Integer getQuantite() { return quantiteDemande; }
}
