package com.salma.mini_projet_pharmacie.controller;

import com.salma.mini_projet_pharmacie.dto.OrdonnanceDTO;
import com.salma.mini_projet_pharmacie.model.Client;
import com.salma.mini_projet_pharmacie.model.Ordonnance;
import com.salma.mini_projet_pharmacie.repository.OrdonnanceRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ordonnances")
public class OrdonnanceController {

    private final OrdonnanceRepository ordonnanceRepository;

    public OrdonnanceController(OrdonnanceRepository ordonnanceRepository) {
        this.ordonnanceRepository = ordonnanceRepository;
    }

    @PostMapping
    public OrdonnanceDTO creerOrdonnance(@RequestBody OrdonnanceDTO dto) {
        if (dto.getClientId() == null) throw new RuntimeException("Client obligatoire");
        if (dto.getDateEmission() == null) throw new RuntimeException("Date émission obligatoire");
        if (dto.getNomMedecin() == null || dto.getNomMedecin().isBlank()) throw new RuntimeException("Nom médecin obligatoire");

        Ordonnance o = new Ordonnance();
        o.setDateEmission(dto.getDateEmission());
        o.setNomMedecin(dto.getNomMedecin());
        o.setDescription(dto.getDescription());

        Client c = new Client();
        c.setId(dto.getClientId());
        o.setClient(c);

        Ordonnance saved = ordonnanceRepository.save(o);

        OrdonnanceDTO res = new OrdonnanceDTO();
        res.setIdOrdonnance(saved.getIdOrdonnance());
        res.setDateEmission(saved.getDateEmission());
        res.setNomMedecin(saved.getNomMedecin());
        res.setDescription(saved.getDescription());
        res.setClientId(dto.getClientId());

        return res;
    }

    @GetMapping("/client/{id}")
    public List<OrdonnanceDTO> ordonnancesParClient(@PathVariable Integer id) {
        return ordonnanceRepository.findByClientId(id)
                .stream()
                .map(o -> {
                    OrdonnanceDTO d = new OrdonnanceDTO();
                    d.setIdOrdonnance(o.getIdOrdonnance());
                    d.setDateEmission(o.getDateEmission());
                    d.setNomMedecin(o.getNomMedecin());
                    d.setDescription(o.getDescription());
                    d.setClientId(id);
                    return d;
                })
                .collect(Collectors.toList());
    }
}
