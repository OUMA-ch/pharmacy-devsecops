package com.salma.mini_projet_pharmacie.mapper;

import com.salma.mini_projet_pharmacie.dto.LigneCommandeDTO;
import com.salma.mini_projet_pharmacie.model.LigneCommande;
import com.salma.mini_projet_pharmacie.model.Produit;

public class LigneCommandeMapper {

    public static LigneCommandeDTO toDTO(LigneCommande lc) {
        if (lc == null) return null;

        LigneCommandeDTO dto = new LigneCommandeDTO();

        if (lc.getProduit() != null) {
            dto.setProduitId(lc.getProduit().getIdProduit());
        }

        dto.setQuantiteDemande(lc.getQuantiteDemande());

        return dto;
    }

    public static LigneCommande toEntity(LigneCommandeDTO dto) {
        if (dto == null) return null;

        LigneCommande lc = new LigneCommande();

        // produit (id seulement)
        if (dto.getProduitId() != null) {
            Produit p = new Produit();
            p.setIdProduit(dto.getProduitId());
            lc.setProduit(p);
        }

        lc.setQuantiteDemande(dto.getQuantiteDemande());

        // IMPORTANT: commande sera définie dans CommandeMapper (lien bidirectionnel)
        lc.setCommande(null);

        return lc;
    }
}
