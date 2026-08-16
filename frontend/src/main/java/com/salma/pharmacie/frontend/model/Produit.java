package com.salma.pharmacie.frontend.model;

import com.google.gson.annotations.SerializedName;

public class Produit {

    // Backend: idProduit
    @SerializedName("idProduit")
    private Integer id;

    // Backend: nomCommercial
    @SerializedName("nomCommercial")
    private String nom;

    // Backend: composition (ou description)
    @SerializedName("composition")
    private String description;

    // Backend: prixP
    @SerializedName("prixP")
    private Double prix;

    // Backend: quantiteStock
    @SerializedName("quantiteStock")
    private Integer quantiteStock;

    // Backend renvoie souvent "2026-12-01T00:00:00.000Z" => on garde String
    @SerializedName("datePeremption")
    private String datePeremption;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }

    public Integer getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(Integer quantiteStock) { this.quantiteStock = quantiteStock; }

    public String getDatePeremption() { return datePeremption; }
    public void setDatePeremption(String datePeremption) { this.datePeremption = datePeremption; }

    // ✅ Pour la colonne "Date péremption" (affichage)
    public String getDatePeremptionAffiche() {
        if (datePeremption == null) return "";
        return datePeremption.length() >= 10 ? datePeremption.substring(0, 10) : datePeremption;
    }
}
