package com.salma.mini_projet_pharmacie.controller;

import com.salma.mini_projet_pharmacie.dto.ProduitDTO;
import com.salma.mini_projet_pharmacie.service.ProduitService;
import com.salma.mini_projet_pharmacie.utils.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService produitService;

    @PostMapping
    public ResponseEntity<?> ajouter(@RequestBody ProduitDTO dto) {
        return ResponseHandler.success("Produit ajouté avec succès", produitService.ajouterProduit(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modifier(@PathVariable Integer id, @RequestBody ProduitDTO dto) {
        return ResponseHandler.success("Produit mis à jour", produitService.modifierProduit(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimer(@PathVariable Integer id) {
        produitService.supprimerProduit(id);
        return ResponseHandler.success("Produit supprimé", null);
    }

    @GetMapping
    public ResponseEntity<?> all() {
        return ResponseHandler.success("Liste des produits", produitService.all());
    }

    @GetMapping("/alerte-stock")
    public ResponseEntity<?> faibleStock() {
        return ResponseHandler.success("Produits en stock faible", produitService.produitsStockFaible());
    }

    @GetMapping("/peremption")
    public ResponseEntity<?> perimes() {
        return ResponseHandler.success("Produits périmés", produitService.produitsPerimes());
    }
}