package com.salma.mini_projet_pharmacie.mapper;

import com.salma.mini_projet_pharmacie.dto.VenteDTO;
import com.salma.mini_projet_pharmacie.model.Client;
import com.salma.mini_projet_pharmacie.model.Ordonnance;
import com.salma.mini_projet_pharmacie.model.Produit;
import com.salma.mini_projet_pharmacie.model.Vente;

public class VenteMapper {

    public static Vente toEntity(VenteDTO dto) {
        Vente v = new Vente();
        v.setIdVente(dto.getIdVente());
        v.setQuantite(dto.getQuantite());
        v.setDateVente(dto.getDateVente()); // (souvent null à la création)

        if (dto.getClientId() != null) {
            Client c = new Client();
            c.setId(dto.getClientId());
            v.setClient(c);
        }

        if (dto.getProduitId() != null) {
            Produit p = new Produit();
            p.setIdProduit(dto.getProduitId());
            v.setProduit(p);
        }

        // ordonnance optionnelle (id seulement, chargée ensuite dans service)
        if (dto.getOrdonnanceId() != null) {
            Ordonnance o = new Ordonnance();
            o.setIdOrdonnance(dto.getOrdonnanceId());
            v.setOrdonnance(o);
        } else {
            v.setOrdonnance(null);
        }

        return v;
    }

    public static VenteDTO toDTO(Vente v) {
        VenteDTO dto = new VenteDTO();
        dto.setIdVente(v.getIdVente());
        dto.setQuantite(v.getQuantite());
        dto.setDateVente(v.getDateVente());

        if (v.getClient() != null) dto.setClientId(v.getClient().getId());
        if (v.getProduit() != null) dto.setProduitId(v.getProduit().getIdProduit());

        if (v.getOrdonnance() != null) {
            dto.setOrdonnanceId(v.getOrdonnance().getIdOrdonnance());

            // ✅ infos ordonnance pour affichage
            dto.setOrdonnanceNomMedecin(v.getOrdonnance().getNomMedecin());
            dto.setOrdonnanceDateEmission(v.getOrdonnance().getDateEmission());
            dto.setOrdonnanceDescription(v.getOrdonnance().getDescription());
        }

        // ✅ prix total calculé
        if (v.getProduit() != null) {
            dto.setPrixTotal(v.getQuantite() * v.getProduit().getPrixP());
        } else {
            dto.setPrixTotal(0);
        }

        return dto;
    }
}
