package com.salma.pharmacie.frontend.model;

public class Vente {
    private Integer clientId;
    private String dateVente;
    private Integer idVente;
    private Integer ordonnanceId;
    private double prixTotal;
    private Integer produitId;
    private int quantite;
    private String ordonnanceNomMedecin;
    private String ordonnanceDateEmission;
    private String ordonnanceDescription;

    public String getOrdonnanceNomMedecin() { return ordonnanceNomMedecin; }
    public void setOrdonnanceNomMedecin(String ordonnanceNomMedecin) { this.ordonnanceNomMedecin = ordonnanceNomMedecin; }

    public String getOrdonnanceDateEmission() { return ordonnanceDateEmission; }
    public void setOrdonnanceDateEmission(String ordonnanceDateEmission) { this.ordonnanceDateEmission = ordonnanceDateEmission; }

    public String getOrdonnanceDescription() { return ordonnanceDescription; }
    public void setOrdonnanceDescription(String ordonnanceDescription) { this.ordonnanceDescription = ordonnanceDescription; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public String getDateVente() { return dateVente; }
    public void setDateVente(String dateVente) { this.dateVente = dateVente; }

    public Integer getIdVente() { return idVente; }
    public void setIdVente(Integer idVente) { this.idVente = idVente; }

    public Integer getOrdonnanceId() { return ordonnanceId; }
    public void setOrdonnanceId(Integer ordonnanceId) { this.ordonnanceId = ordonnanceId; }

    public double getPrixTotal() { return prixTotal; }
    public void setPrixTotal(double prixTotal) { this.prixTotal = prixTotal; }

    public Integer getProduitId() { return produitId; }
    public void setProduitId(Integer produitId) { this.produitId = produitId; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public String getDateVenteAffiche() {
        if (dateVente == null) return "";
        return dateVente.length() >= 10 ? dateVente.substring(0, 10) : dateVente;
    }
}
