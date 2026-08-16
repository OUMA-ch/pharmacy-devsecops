package com.salma.mini_projet_pharmacie.controller;

import com.salma.mini_projet_pharmacie.service.FournitureService;
import com.salma.mini_projet_pharmacie.utils.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fournitures")
@RequiredArgsConstructor
public class FournitureController {

    private final FournitureService fournitureService;

    @PostMapping("/{idProduit}/{idFournisseur}")
    public ResponseEntity<?> ajouterLien(
            @PathVariable Integer idProduit,
            @PathVariable Integer idFournisseur,
            @RequestParam Double prixAchat
    ) {
        return ResponseHandler.success(
                "Lien produit-fournisseur ajouté",
                fournitureService.ajouterFourniture(idProduit, idFournisseur, prixAchat)
        );
    }

    // ✅ LISTE des produits vendus par un fournisseur
    @GetMapping("/fournisseur/{idFournisseur}")
    public ResponseEntity<?> listeParFournisseur(@PathVariable Integer idFournisseur) {
        return ResponseHandler.success(
                "Liste fournitures du fournisseur",
                fournitureService.listeParFournisseur(idFournisseur)
        );
    }

    // ✅ MODIFIER prix achat
    @PutMapping("/{idProduit}/{idFournisseur}")
    public ResponseEntity<?> modifierPrix(
            @PathVariable Integer idProduit,
            @PathVariable Integer idFournisseur,
            @RequestParam Double prixAchat
    ) {
        return ResponseHandler.success(
                "Prix achat modifié",
                fournitureService.modifierPrix(idProduit, idFournisseur, prixAchat)
        );
    }

    //  SUPPRIMER lien
    @DeleteMapping("/{idProduit}/{idFournisseur}")
    public ResponseEntity<?> supprimer(
            @PathVariable Integer idProduit,
            @PathVariable Integer idFournisseur
    ) {
        fournitureService.supprimer(idProduit, idFournisseur);
        return ResponseHandler.success("Lien supprimé", null);
    }
}
