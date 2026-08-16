package com.salma.mini_projet_pharmacie.mapper;

import com.salma.mini_projet_pharmacie.dto.ProduitDTO;
import com.salma.mini_projet_pharmacie.model.Produit;

public class ProduitMapper {

    public static ProduitDTO toDTO(Produit produit) {
        ProduitDTO dto = new ProduitDTO();

        dto.setIdProduit(produit.getIdProduit());
        dto.setNomCommercial(produit.getNomCommercial());
        dto.setComposition(produit.getComposition());
        dto.setPrixP(produit.getPrixP());
        dto.setFormPharmaceutique(produit.getFormPharmaceutique());
        dto.setDosage(produit.getDosage());
        dto.setDatePeremption(produit.getDatePeremption());
        dto.setQuantiteStock(produit.getQuantiteStock());

        return dto;
    }

    public static Produit toEntity(ProduitDTO dto) {
        return Produit.builder()
                .idProduit(dto.getIdProduit())
                .nomCommercial(dto.getNomCommercial())
                .composition(dto.getComposition())
                .prixP(dto.getPrixP())
                .formPharmaceutique(dto.getFormPharmaceutique())
                .dosage(dto.getDosage())
                .datePeremption(dto.getDatePeremption())
                .quantiteStock(dto.getQuantiteStock())
                .build();
    }
}