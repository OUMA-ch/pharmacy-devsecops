package com.salma.mini_projet_pharmacie.mapper;

import com.salma.mini_projet_pharmacie.dto.OrdonnanceDTO;
import com.salma.mini_projet_pharmacie.model.Client;
import com.salma.mini_projet_pharmacie.model.Ordonnance;

public class OrdonnanceMapper {

    public static Ordonnance toEntity(OrdonnanceDTO dto) {
        Ordonnance o = new Ordonnance();
        o.setIdOrdonnance(dto.getIdOrdonnance());
        o.setDateEmission(dto.getDateEmission());
        o.setNomMedecin(dto.getNomMedecin());
        o.setDescription(dto.getDescription());

        Client c = new Client();
        c.setId(dto.getClientId());
        o.setClient(c);

        return o;
    }

    public static OrdonnanceDTO toDTO(Ordonnance o) {
        OrdonnanceDTO dto = new OrdonnanceDTO();
        dto.setIdOrdonnance(o.getIdOrdonnance());
        dto.setDateEmission(o.getDateEmission());
        dto.setNomMedecin(o.getNomMedecin());
        dto.setDescription(o.getDescription());
        dto.setClientId(o.getClient().getId());
        return dto;
    }
}
