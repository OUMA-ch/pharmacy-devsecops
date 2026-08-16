// src/main/java/com/salma/mini_projet_pharmacie/service/ReportService.java
package com.salma.mini_projet_pharmacie.service;

import com.salma.mini_projet_pharmacie.dto.ChartPointDTO;
import com.salma.mini_projet_pharmacie.model.Produit;
import com.salma.mini_projet_pharmacie.model.Vente;
import com.salma.mini_projet_pharmacie.repository.ProduitRepository;
import com.salma.mini_projet_pharmacie.repository.VenteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ReportService {

    private final VenteRepository venteRepository;
    private final ProduitRepository produitRepository;

    public ReportService(VenteRepository venteRepository,
                         ProduitRepository produitRepository) {
        this.venteRepository = venteRepository;
        this.produitRepository = produitRepository;
    }

    /**
     * type:
     *  - VENTES_PRODUIT
     *  - STOCK_PRODUIT
     *  - STOCK_FAIBLE
     *
     * metric:
     *  - QTE (ventes)
     *  - CA  (ventes)
     *  - STOCK (stock)
     */
    public List<ChartPointDTO> getBarData(
            String type,
            String metric,
            LocalDate from,
            LocalDate to,
            Integer top,
            Integer seuilStockFaible
    ) {
        String t = (type == null) ? "" : type.trim().toUpperCase();
        String m = (metric == null) ? "" : metric.trim().toUpperCase();

        int topN = (top == null || top <= 0) ? 10 : top;
        int seuil = (seuilStockFaible == null || seuilStockFaible < 0) ? 10 : seuilStockFaible;

        return switch (t) {
            case "VENTES_PRODUIT" -> ventesParProduit(m, from, to, topN);
            case "STOCK_PRODUIT" -> stockParProduit(topN);
            case "STOCK_FAIBLE" -> stockFaible(seuil, topN);
            default -> throw new IllegalArgumentException("Type de rapport invalide: " + type);
        };
    }

    private List<ChartPointDTO> ventesParProduit(String metric, LocalDate from, LocalDate to, int topN) {
        List<Vente> ventes = venteRepository.findAll();

        Stream<Vente> st = ventes.stream();
        if (from != null) st = st.filter(v -> v.getDateVente() != null && !v.getDateVente().isBefore(from));
        if (to != null) st = st.filter(v -> v.getDateVente() != null && !v.getDateVente().isAfter(to));

        Map<String, Double> agg = new HashMap<>();

        st.forEach(v -> {
            String label = "Produit ?";
            if (v.getProduit() != null) {
                // adapte si ton Produit a un autre getter
                String nom = v.getProduit().getNomCommercial();
                if (nom != null && !nom.isBlank()) label = nom;
                else label = "Produit#" + v.getProduit().getIdProduit();
            }

            double val;
            if ("CA".equals(metric)) {
                // calculerPrixTotal() existe chez toi (déjà utilisé dans VenteMapper)
                val = v.calculerPrixTotal();
            } else {
                // default: quantité
                val = (double) v.getQuantite();
            }

            agg.merge(label, val, Double::sum);
        });

        return agg.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .map(e -> new ChartPointDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<ChartPointDTO> stockParProduit(int topN) {
        List<Produit> produits = produitRepository.findAll();

        return produits.stream()
                .map(p -> {
                    String label = (p.getNomCommercial() != null && !p.getNomCommercial().isBlank())
                            ? p.getNomCommercial()
                            : "Produit#" + p.getIdProduit();
                    return new ChartPointDTO(label, (double) p.getQuantiteStock());
                })
                .sorted(Comparator.comparing(ChartPointDTO::getValue).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    private List<ChartPointDTO> stockFaible(int seuil, int topN) {
        List<Produit> produits = produitRepository.findAll();

        return produits.stream()
                .filter(p -> p.getQuantiteStock() <= seuil)
                .map(p -> {
                    String label = (p.getNomCommercial() != null && !p.getNomCommercial().isBlank())
                            ? p.getNomCommercial()
                            : "Produit#" + p.getIdProduit();
                    return new ChartPointDTO(label, (double) p.getQuantiteStock());
                })
                .sorted(Comparator.comparing(ChartPointDTO::getValue)) // faible -> plus faible
                .limit(topN)
                .collect(Collectors.toList());
    }

    // (optionnel) si tu veux garder les anciens endpoints "bruts"
    public List<Vente> getRapportVentesRaw() { return venteRepository.findAll(); }
    public List<Produit> getRapportStockRaw() { return produitRepository.findAll(); }
}
