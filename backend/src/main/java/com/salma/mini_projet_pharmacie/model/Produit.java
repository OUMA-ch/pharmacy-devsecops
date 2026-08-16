package com.salma.mini_projet_pharmacie.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "produit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProduit")
    private Integer idProduit;

    @Column(name = "nomCommercial")
    private String nomCommercial;

    @Column(name = "composition")
    private String composition;

    @Column(name = "prixP")
    private Double prixP;

    @Column(name = "formPharmaceutique")
    private String formPharmaceutique;

    @Column(name = "dosage")
    private String dosage;

    @Column(name = "datePeremption")
    private Date datePeremption;

    @Column(name = "quantiteStock")
    private Integer quantiteStock;

    @OneToMany(mappedBy = "produit")
    private List<Fourniture> fournitures;
// ------------------------- GETTERS & SETTERS -------------------------

    public Integer getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(Integer idProduit) {
        this.idProduit = idProduit;
    }

    public String getNomCommercial() {
        return nomCommercial;
    }

    public void setNomCommercial(String nomCommercial) {
        this.nomCommercial = nomCommercial;
    }

    public String getComposition() {
        return composition;
    }

    public void setComposition(String composition) {
        this.composition = composition;
    }

    public Double getPrixP() {
        return prixP;
    }

    public void setPrixP(Double prixP) {
        this.prixP = prixP;
    }

    public String getFormPharmaceutique() {
        return formPharmaceutique;
    }

    public void setFormPharmaceutique(String formPharmaceutique) {
        this.formPharmaceutique = formPharmaceutique;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public Date getDatePeremption() {
        return datePeremption;
    }

    public void setDatePeremption(Date datePeremption) {
        this.datePeremption = datePeremption;
    }

    public Integer getQuantiteStock() {
        return quantiteStock;
    }

    public void setQuantiteStock(Integer quantiteStock) {
        this.quantiteStock = quantiteStock;
    }

    public List<Fourniture> getFournitures() {
        return fournitures;
    }

    public void setFournitures(List<Fourniture> fournitures) {
        this.fournitures = fournitures;
    }
}
