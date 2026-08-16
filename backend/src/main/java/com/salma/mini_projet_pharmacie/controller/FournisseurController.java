package com.salma.mini_projet_pharmacie.controller;


import com.salma.mini_projet_pharmacie.dto.FournisseurDTO;
import com.salma.mini_projet_pharmacie.service.FournisseurService;
import com.salma.mini_projet_pharmacie.utils.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fournisseurs")
@RequiredArgsConstructor
public class FournisseurController {

    private final FournisseurService fournisseurService;

    @PostMapping
    public ResponseEntity<?> ajouter(@RequestBody FournisseurDTO dto) {
        return ResponseHandler.success("Fournisseur ajouté", fournisseurService.ajouter(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modifier(@PathVariable Integer id, @RequestBody FournisseurDTO dto) {
        return ResponseHandler.success("Fournisseur modifié", fournisseurService.modifier(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimer(@PathVariable Integer id) {
        fournisseurService.supprimer(id);
        return ResponseHandler.success("Fournisseur supprimé", null);
    }

    @GetMapping
    public ResponseEntity<?> liste() {
        return ResponseHandler.success("Liste des fournisseurs", fournisseurService.liste());
    }
}
