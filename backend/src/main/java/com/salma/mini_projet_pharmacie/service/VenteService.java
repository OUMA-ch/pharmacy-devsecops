package com.salma.mini_projet_pharmacie.service;

import com.salma.mini_projet_pharmacie.dto.VenteDTO;
import com.salma.mini_projet_pharmacie.exception.StockInsuffisantException;
import com.salma.mini_projet_pharmacie.mapper.VenteMapper;
import com.salma.mini_projet_pharmacie.model.Client;
import com.salma.mini_projet_pharmacie.model.Ordonnance;
import com.salma.mini_projet_pharmacie.model.Produit;
import com.salma.mini_projet_pharmacie.model.Vente;
import com.salma.mini_projet_pharmacie.repository.OrdonnanceRepository;
import com.salma.mini_projet_pharmacie.repository.ProduitRepository;
import com.salma.mini_projet_pharmacie.repository.VenteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VenteService {

    private final VenteRepository venteRepository;
    private final ProduitRepository produitRepository;
    private final OrdonnanceRepository ordonnanceRepository;
    private final NotificationService notificationService;

    public VenteService(VenteRepository venteRepository,
                        ProduitRepository produitRepository,
                        OrdonnanceRepository ordonnanceRepository,
                        NotificationService notificationService) {
        this.venteRepository = venteRepository;
        this.produitRepository = produitRepository;
        this.ordonnanceRepository = ordonnanceRepository;
        this.notificationService = notificationService;
    }

    public VenteDTO enregistrerVente(VenteDTO dto) {
        Vente vente = VenteMapper.toEntity(dto);

        if (vente.getProduit() == null || vente.getProduit().getIdProduit() == null) {
            throw new RuntimeException("Produit obligatoire");
        }
        if (vente.getClient() == null || vente.getClient().getId() == null) {
            throw new RuntimeException("Client obligatoire");
        }
        if (vente.getQuantite() <= 0) {
            throw new RuntimeException("Quantité invalide");
        }

        // 1) Charger produit réel
        Produit produit = produitRepository.findById(vente.getProduit().getIdProduit())
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        // 2) Ordonnance optionnelle + règle 3 mois (AVANT stock)
        if (dto.getOrdonnanceId() != null) {
            Ordonnance ord = ordonnanceRepository.findById(dto.getOrdonnanceId())
                    .orElseThrow(() -> new RuntimeException("Ordonnance introuvable"));

            if (ord.getDateEmission() == null) {
                throw new RuntimeException("Date d'émission manquante pour l'ordonnance");
            }

            // ✅ refus si plus ancienne que 3 mois
            if (ord.getDateEmission().isBefore(LocalDate.now().minusMonths(3))) {
                throw new RuntimeException("Ordonnance expirée (plus de 3 mois)");
            }

            vente.setOrdonnance(ord);
        } else {
            vente.setOrdonnance(null);
        }

        // 3) Stock check
        if (produit.getQuantiteStock() < vente.getQuantite()) {
            throw new StockInsuffisantException("Stock insuffisant");
        }

        // 4) Décrémenter stock
        produit.setQuantiteStock(produit.getQuantiteStock() - vente.getQuantite());
        produitRepository.save(produit);

        vente.setProduit(produit);
        vente.setDateVente(LocalDate.now());

        Vente saved = venteRepository.save(vente);

        notificationService.notifierClient(
                saved.getClient(),
                "Confirmation de votre achat",
                "Votre achat a été effectué avec succès.\n\n" +
                        "Produit ID : " + produit.getIdProduit() + "\n" +
                        "Quantité : " + saved.getQuantite() + "\n" +
                        "Prix total : " + saved.calculerPrixTotal() + " DH\n" +
                        "Date : " + saved.getDateVente()
        );
        return VenteMapper.toDTO(saved);
    }

    public List<VenteDTO> ventesParClient(Integer clientId) {
        Client c = new Client();
        c.setId(clientId);

        return venteRepository.findByClient(c)
                .stream()
                .map(VenteMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ✅ UPDATE : ajuste stock + vérifie ordonnance 3 mois
    public VenteDTO modifierVente(Integer idVente, VenteDTO dto) {
        Vente existing = venteRepository.findById(idVente)
                .orElseThrow(() -> new RuntimeException("Vente introuvable"));

        Produit produit = existing.getProduit();
        if (produit == null) throw new RuntimeException("Produit introuvable pour cette vente");

        int oldQ = existing.getQuantite();
        int newQ = dto.getQuantite();

        if (newQ <= 0) throw new RuntimeException("Quantité invalide");

        // 1) Update ordonnance + règle 3 mois (AVANT stock)
        if (dto.getOrdonnanceId() != null) {
            Ordonnance ord = ordonnanceRepository.findById(dto.getOrdonnanceId())
                    .orElseThrow(() -> new RuntimeException("Ordonnance introuvable"));

            if (ord.getDateEmission() == null) {
                throw new RuntimeException("Date d'émission manquante pour l'ordonnance");
            }

            if (ord.getDateEmission().isBefore(LocalDate.now().minusMonths(3))) {
                throw new RuntimeException("Ordonnance expirée (plus de 3 mois)");
            }

            existing.setOrdonnance(ord);
        } else {
            existing.setOrdonnance(null);
        }

        // 2) Ajuster stock en fonction de la différence
        int diff = newQ - oldQ; // diff>0 => besoin de plus de stock
        if (diff > 0) {
            if (produit.getQuantiteStock() < diff) {
                throw new StockInsuffisantException("Stock insuffisant");
            }
            produit.setQuantiteStock(produit.getQuantiteStock() - diff);
        } else if (diff < 0) {
            produit.setQuantiteStock(produit.getQuantiteStock() + (-diff));
        }

        produitRepository.save(produit);

        // 3) Update quantité
        existing.setQuantite(newQ);

        Vente saved = venteRepository.save(existing);
        return VenteMapper.toDTO(saved);
    }

    // ✅ DELETE : remet stock puis supprime
    public void supprimerVente(Integer idVente) {
        Vente existing = venteRepository.findById(idVente)
                .orElseThrow(() -> new RuntimeException("Vente introuvable"));

        Produit produit = existing.getProduit();
        if (produit != null) {
            produit.setQuantiteStock(produit.getQuantiteStock() + existing.getQuantite());
            produitRepository.save(produit);
        }

        venteRepository.delete(existing);
    }
    public List<VenteDTO> toutesLesVentes() {
        return venteRepository.findAll()
                .stream()
                .map(VenteMapper::toDTO)
                .collect(Collectors.toList());
    }
}
