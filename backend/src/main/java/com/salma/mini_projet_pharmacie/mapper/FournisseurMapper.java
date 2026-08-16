package com.salma.mini_projet_pharmacie.mapper;

import com.salma.mini_projet_pharmacie.dto.FournisseurDTO;
import com.salma.mini_projet_pharmacie.model.Fournisseur;

public class FournisseurMapper {

    public static FournisseurDTO toDTO(Fournisseur fournisseur) {
        FournisseurDTO dto = new FournisseurDTO();

        dto.setIdFournisseur(fournisseur.getIdFournisseur());
        dto.setNomFournisseur(fournisseur.getNomFournisseur());
        dto.setTel(fournisseur.getTel());

        return dto;
    }

    public static Fournisseur toEntity(FournisseurDTO dto) {
        return Fournisseur.builder()
                .idFournisseur(dto.getIdFournisseur())
                .nomFournisseur(dto.getNomFournisseur())
                .tel(dto.getTel())
                .build();
    }
}
