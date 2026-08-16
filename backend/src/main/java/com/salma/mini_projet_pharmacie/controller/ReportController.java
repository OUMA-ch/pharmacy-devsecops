// src/main/java/com/salma/mini_projet_pharmacie/controller/ReportController.java
package com.salma.mini_projet_pharmacie.controller;

import com.salma.mini_projet_pharmacie.dto.ChartPointDTO;
import com.salma.mini_projet_pharmacie.model.Produit;
import com.salma.mini_projet_pharmacie.model.Vente;
import com.salma.mini_projet_pharmacie.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // ✅ Nouveau: data prête pour BarChart
    // Ex: /reports/bar?type=VENTES_PRODUIT&metric=CA&from=2025-12-01&to=2025-12-31&top=10
    // Ex: /reports/bar?type=STOCK_FAIBLE&seuil=5&top=20
    @GetMapping("/bar")
    public List<ChartPointDTO> bar(
            @RequestParam String type,
            @RequestParam(required = false) String metric,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer top,
            @RequestParam(required = false) Integer seuil
    ) {
        return reportService.getBarData(type, metric, from, to, top, seuil);
    }

    // (optionnel) garder l’existant brut
    @GetMapping("/ventes")
    public List<Vente> rapportVentesRaw() {
        return reportService.getRapportVentesRaw();
    }

    @GetMapping("/stock")
    public List<Produit> rapportStockRaw() {
        return reportService.getRapportStockRaw();
    }
}
