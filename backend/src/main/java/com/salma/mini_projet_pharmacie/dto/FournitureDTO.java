package com.salma.mini_projet_pharmacie.dto;

import lombok.Data;

@Data
public class FournitureDTO {

    private Integer idProduit;
    private Integer idFournisseur;
    private Double prixAchat;

    //  utile pour l'affichage côté front
    private String nomProduit;
}
