package com.salma.mini_projet_pharmacie.mapper;

import com.salma.mini_projet_pharmacie.dto.CommandeDTO;
import com.salma.mini_projet_pharmacie.dto.LigneCommandeDTO;
import com.salma.mini_projet_pharmacie.model.Commande;
import com.salma.mini_projet_pharmacie.model.Fournisseur;
import com.salma.mini_projet_pharmacie.model.LigneCommande;

import java.util.List;
import java.util.stream.Collectors;

public class CommandeMapper {

    public static CommandeDTO toDTO(Commande c) {
        if (c == null) return null;

        CommandeDTO dto = new CommandeDTO();
        dto.setIdCommande(c.getNumCmd());
        dto.setDateCommande(c.getDateCommande());   // ✅
        dto.setStatut(c.getStatut());

        if (c.getFournisseur() != null) {
            dto.setFournisseurId(c.getFournisseur().getIdFournisseur());
        }

        if (c.getLignes() != null) {
            List<LigneCommandeDTO> lignes = c.getLignes().stream()
                    .map(LigneCommandeMapper::toDTO)
                    .collect(Collectors.toList());
            dto.setLignes(lignes);
        }

        return dto;
    }

    public static Commande toEntity(CommandeDTO dto) {
        if (dto == null) return null;

        Commande c = new Commande();
        c.setNumCmd(dto.getIdCommande());
        c.setDateCommande(dto.getDateCommande());  // ✅ (souvent null, service peut mettre now())
        c.setStatut(dto.getStatut());

        if (dto.getFournisseurId() != null) {
            Fournisseur f = new Fournisseur();
            f.setIdFournisseur(dto.getFournisseurId());
            c.setFournisseur(f);
        }

        if (dto.getLignes() != null) {
            List<LigneCommande> lignes = dto.getLignes().stream()
                    .map(LigneCommandeMapper::toEntity)
                    .collect(Collectors.toList());

            // ✅ important: lier les lignes à la commande
            lignes.forEach(l -> l.setCommande(c));
            c.setLignes(lignes);
        }

        return c;
    }
}
