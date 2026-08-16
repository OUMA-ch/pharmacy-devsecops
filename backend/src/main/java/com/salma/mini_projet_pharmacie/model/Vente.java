package com.salma.mini_projet_pharmacie.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vente")
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idVente;

    private int quantite;
    private LocalDate dateVente;

    @ManyToOne
    @JoinColumn(name = "idClient")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "idProduit")
    private Produit produit;

    @ManyToOne
    @JoinColumn(name = "idOrdonnance")
    private Ordonnance ordonnance;

    // MÉTHODE MÉTIER
    public double calculerPrixTotal() {
        return quantite * produit.getPrixP();
    }

    // GETTERS & SETTERS
    public Integer getIdVente() { return idVente; }
    public void setIdVente(Integer idVente) { this.idVente = idVente; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public LocalDate getDateVente() { return dateVente; }
    public void setDateVente(LocalDate dateVente) { this.dateVente = dateVente; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }

    public Ordonnance getOrdonnance() { return ordonnance; }
    public void setOrdonnance(Ordonnance ordonnance) { this.ordonnance = ordonnance; }
}
