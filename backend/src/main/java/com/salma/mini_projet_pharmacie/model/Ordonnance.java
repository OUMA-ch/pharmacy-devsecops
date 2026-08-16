package com.salma.mini_projet_pharmacie.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "ordonnance")
public class Ordonnance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idOrdonnance;

    private LocalDate dateEmission;
    private String nomMedecin;
    private String description;

    @ManyToOne
    @JoinColumn(name = "idUser")
    private Client client;

    @OneToMany(mappedBy = "ordonnance")
    private List<Vente> ventes;

    // GETTERS & SETTERS
    public Integer getIdOrdonnance() { return idOrdonnance; }
    public void setIdOrdonnance(Integer idOrdonnance) { this.idOrdonnance = idOrdonnance; }

    public LocalDate getDateEmission() { return dateEmission; }
    public void setDateEmission(LocalDate dateEmission) { this.dateEmission = dateEmission; }

    public String getNomMedecin() { return nomMedecin; }
    public void setNomMedecin(String nomMedecin) { this.nomMedecin = nomMedecin; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public List<Vente> getVentes() { return ventes; }
    public void setVentes(List<Vente> ventes) { this.ventes = ventes; }
}
