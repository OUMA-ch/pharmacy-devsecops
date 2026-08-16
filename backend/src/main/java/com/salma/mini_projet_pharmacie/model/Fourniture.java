package com.salma.mini_projet_pharmacie.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fourniture")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fourniture {

    @EmbeddedId
    private FournitureKey id;

    @ManyToOne
    @MapsId("idProduit")
    @JoinColumn(name = "idProduit")
    private Produit produit;

    @ManyToOne
    @MapsId("idFournisseur")
    @JoinColumn(name = "idFournisseur")
    private Fournisseur fournisseur;

    @Column(name = "prixAchat")
    private Double prixAchat;
// ------------------------- GETTERS & SETTERS -------------------------

    public FournitureKey getId() {
        return id;
    }

    public void setId(FournitureKey id) {
        this.id = id;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    public Double getPrixAchat() {
        return prixAchat;
    }

    public void setPrixAchat(Double prixAchat) {
        this.prixAchat = prixAchat;
    }}
