package com.salma.mini_projet_pharmacie.service;

import com.salma.mini_projet_pharmacie.dto.CommandeDTO;
import com.salma.mini_projet_pharmacie.exception.ResourceNotFoundException;
import com.salma.mini_projet_pharmacie.mapper.CommandeMapper;
import com.salma.mini_projet_pharmacie.model.*;
import com.salma.mini_projet_pharmacie.repository.CommandeRepository;
import com.salma.mini_projet_pharmacie.repository.FournisseurRepository;
import com.salma.mini_projet_pharmacie.repository.ProduitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ProduitRepository produitRepository;
    private final FournisseurRepository fournisseurRepository;

    public CommandeService(CommandeRepository commandeRepository,
                           ProduitRepository produitRepository,
                           FournisseurRepository fournisseurRepository) {
        this.commandeRepository = commandeRepository;
        this.produitRepository = produitRepository;
        this.fournisseurRepository = fournisseurRepository;
    }

    // =========================
    // Création commande (DTO)
    // =========================
    public Commande creerCommande(CommandeDTO dto) {

        if (dto.getFournisseurId() == null) {
            throw new RuntimeException("ID Fournisseur manquant");
        }

        if (dto.getLignes() == null || dto.getLignes().isEmpty()) {
            throw new RuntimeException("La commande doit contenir au moins une ligne");
        }

        Fournisseur fournisseur = fournisseurRepository
                .findById(dto.getFournisseurId())
                .orElseThrow(() -> new RuntimeException("Fournisseur introuvable"));

        // Mapper -> entity
        Commande commande = CommandeMapper.toEntity(dto);

        // ✅ date + statut par défaut
        if (commande.getDateCommande() == null) {
            commande.setDateCommande(LocalDate.now());
        }
        if (commande.getStatut() == null || commande.getStatut().isBlank()) {
            commande.setStatut("EN_ATTENTE");
        }

        commande.setFournisseur(fournisseur);

        // lignes: vérifier produit et relier à la commande
        for (LigneCommande ligne : commande.getLignes()) {

            if (ligne.getProduit() == null || ligne.getProduit().getIdProduit() == null) {
                throw new RuntimeException("Produit manquant dans une ligne");
            }

            if (ligne.getQuantiteDemande() == null || ligne.getQuantiteDemande() <= 0) {
                throw new RuntimeException("Quantité demandée invalide");
            }

            Produit produit = produitRepository
                    .findById(ligne.getProduit().getIdProduit())
                    .orElseThrow(() -> new RuntimeException("Produit introuvable"));

            ligne.setProduit(produit);
            ligne.setCommande(commande);
        }

        return commandeRepository.save(commande);
    }

    // =========================
    // Changement de statut
    // =========================
    public Commande changerStatut(Integer numCmd, String newStatut) {

        Commande commande = commandeRepository.findById(numCmd)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        String oldStatut = commande.getStatut();
        if (oldStatut == null) oldStatut = "EN_ATTENTE"; // sécurité

        // rien à faire si pas de changement
        if (oldStatut.equalsIgnoreCase(newStatut)) {
            return commande;
        }

        // lignes obligatoires
        if (commande.getLignes() == null || commande.getLignes().isEmpty()) {
            throw new RuntimeException("Aucune ligne dans cette commande.");
        }

        boolean oldLivree = "LIVREE".equalsIgnoreCase(oldStatut);
        boolean newLivree = "LIVREE".equalsIgnoreCase(newStatut);

        //  Cas 1 : on passe vers LIVREE => +stock
        if (!oldLivree && newLivree) {
            for (LigneCommande lc : commande.getLignes()) {

                if (lc.getProduit() == null || lc.getProduit().getIdProduit() == null) {
                    throw new RuntimeException("Produit manquant dans une ligne commande.");
                }

                Produit produit = produitRepository.findById(lc.getProduit().getIdProduit())
                        .orElseThrow(() -> new RuntimeException("Produit introuvable"));

                produit.setQuantiteStock(produit.getQuantiteStock() + lc.getQuantiteDemande());
                produitRepository.save(produit);
            }
        }

        //  Cas 2 : on quitte LIVREE => -stock (ex: LIVREE -> ANNULEE)
        if (oldLivree && !newLivree) {
            for (LigneCommande lc : commande.getLignes()) {

                if (lc.getProduit() == null || lc.getProduit().getIdProduit() == null) {
                    throw new RuntimeException("Produit manquant dans une ligne commande.");
                }

                Produit produit = produitRepository.findById(lc.getProduit().getIdProduit())
                        .orElseThrow(() -> new RuntimeException("Produit introuvable"));

                int newStock = produit.getQuantiteStock() - lc.getQuantiteDemande();

                //  sécurité : empêcher stock négatif
                if (newStock < 0) {
                    throw new RuntimeException(
                            "Annulation impossible : stock insuffisant pour retirer "
                                    + lc.getQuantiteDemande()
                                    + " du produit ID " + produit.getIdProduit()
                    );
                }

                produit.setQuantiteStock(newStock);
                produitRepository.save(produit);
            }
        }

        commande.setStatut(newStatut);
        return commandeRepository.save(commande);
    }



    //  IMPORTANT : charger avec lignes
    public List<Commande> getAllCommandes() {
        return commandeRepository.findAllWithDetails();
    }

    public void supprimerCommande(Integer numCmd) {
        Commande cmd = commandeRepository.findById(numCmd)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));
        commandeRepository.delete(cmd);
    }
}
