package com.salma.mini_projet_pharmacie.controller;

import com.salma.mini_projet_pharmacie.dto.VenteDTO;
import com.salma.mini_projet_pharmacie.service.VenteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ventes")
public class VenteController {

    private final VenteService venteService;

    public VenteController(VenteService venteService) {
        this.venteService = venteService;
    }

    @PostMapping
    public VenteDTO creer(@RequestBody VenteDTO dto) {
        return venteService.enregistrerVente(dto);
    }

    @GetMapping("/client/{id}")
    public List<VenteDTO> ventesClient(@PathVariable Integer id) {
        return venteService.ventesParClient(id);
    }

    // modifier vente (quantité / ordonnance)
    @PutMapping("/{id}")
    public VenteDTO modifier(@PathVariable Integer id, @RequestBody VenteDTO dto) {
        return venteService.modifierVente(id, dto);
    }

    //  supprimer vente
    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Integer id) {
        venteService.supprimerVente(id);
    }
    @GetMapping
    public List<VenteDTO> all() {
        return venteService.toutesLesVentes();
    }
}
