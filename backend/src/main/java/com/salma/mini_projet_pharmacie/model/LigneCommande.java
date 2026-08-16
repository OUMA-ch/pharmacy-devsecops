package com.salma.mini_projet_pharmacie.model;

import jakarta.persistence.*;

@Entity
@Table(name = "lignecmd")
public class LigneCommande {

    @EmbeddedId
    private LigneCommandeId id = new LigneCommandeId();

    @ManyToOne
    @MapsId("numCmd")
    @JoinColumn(name = "numCmd")
    private Commande commande;

    @ManyToOne
    @MapsId("idProduit")
    @JoinColumn(name = "idProduit")
    private Produit produit;

    @Column(name = "quantite")
    private Integer quantite;

    @Column(name = "quantiteDemande")
    private Integer quantiteDemande;

    public LigneCommandeId getId() {
        return id;
    }

    public void setId(LigneCommandeId id) {
        this.id = id;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public Integer getQuantiteDemande() {
        return quantiteDemande;
    }

    public void setQuantiteDemande(Integer quantiteDemande) {
        this.quantiteDemande = quantiteDemande;
    }
}