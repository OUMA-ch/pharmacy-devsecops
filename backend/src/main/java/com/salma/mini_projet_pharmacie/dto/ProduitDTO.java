package com.salma.mini_projet_pharmacie.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ProduitDTO {

    private Integer idProduit;
    private String nomCommercial;
    private String composition;
    private Double prixP;
    private String formPharmaceutique;
    private String dosage;
    private Date datePeremption;
    private Integer quantiteStock;
}
