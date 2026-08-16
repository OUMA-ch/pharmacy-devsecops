package com.salma.mini_projet_pharmacie.model;

import jakarta.persistence.*;
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "commande")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "numCmd")
    private Integer numCmd;

    @Column(name = "delai")
    private LocalDate dateCommande;

    private String statut; // EN_ATTENTE, LIVREE, ANNULEE

    @ManyToOne
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    @ManyToOne
    @JoinColumn(name = "idPharmacien")
    private Pharmacien pharmacien;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
    private List<LigneCommande> lignes;



    // ===== GETTERS & SETTERS =====

    public Integer getNumCmd() {
        return numCmd;
    }

    public void setNumCmd(Integer numCmd) {
        this.numCmd = numCmd;
    }

    public LocalDate getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }


    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    public List<LigneCommande> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommande> lignes) {
        this.lignes = lignes;
    }


}
