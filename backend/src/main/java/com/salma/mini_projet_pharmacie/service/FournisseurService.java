package com.salma.mini_projet_pharmacie.service;

import com.salma.mini_projet_pharmacie.dto.FournisseurDTO;
import com.salma.mini_projet_pharmacie.exception.NotFoundException;
import com.salma.mini_projet_pharmacie.mapper.FournisseurMapper;
import com.salma.mini_projet_pharmacie.model.Fournisseur;
import com.salma.mini_projet_pharmacie.repository.FournisseurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FournisseurService {

    private final FournisseurRepository fournisseurRepository;

    public FournisseurDTO ajouter(FournisseurDTO dto) {
        Fournisseur saved = fournisseurRepository.save(FournisseurMapper.toEntity(dto));
        return FournisseurMapper.toDTO(saved);
    }

    public FournisseurDTO modifier(Integer id, FournisseurDTO dto) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fourniture introuvable"));


        fournisseur.setNomFournisseur(dto.getNomFournisseur());
        fournisseur.setTel(dto.getTel());

        return FournisseurMapper.toDTO(fournisseurRepository.save(fournisseur));
    }

    public void supprimer(Integer id) {
        fournisseurRepository.deleteById(id);
    }

    public List<FournisseurDTO> liste() {
        return fournisseurRepository.findAll()
                .stream()
                .map(FournisseurMapper::toDTO)
                .toList();
    }
}