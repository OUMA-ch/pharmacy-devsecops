package com.salma.pharmacie.frontend.model;

public class VenteRequest {
    private Integer clientId;
    private Integer produitId;
    private Integer quantite;
    private Integer ordonnanceId; // optionnel

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public Integer getProduitId() { return produitId; }
    public void setProduitId(Integer produitId) { this.produitId = produitId; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public Integer getOrdonnanceId() { return ordonnanceId; }
    public void setOrdonnanceId(Integer ordonnanceId) { this.ordonnanceId = ordonnanceId; }
}
