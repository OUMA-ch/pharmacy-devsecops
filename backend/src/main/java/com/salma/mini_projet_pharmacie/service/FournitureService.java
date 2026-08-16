package com.salma.mini_projet_pharmacie.service;

import com.salma.mini_projet_pharmacie.dto.FournitureDTO;
import com.salma.mini_projet_pharmacie.exception.NotFoundException;
import com.salma.mini_projet_pharmacie.mapper.FournitureMapper;
import com.salma.mini_projet_pharmacie.model.Fournisseur;
import com.salma.mini_projet_pharmacie.model.Fourniture;
import com.salma.mini_projet_pharmacie.model.FournitureKey;
import com.salma.mini_projet_pharmacie.model.Produit;
import com.salma.mini_projet_pharmacie.repository.FournisseurRepository;
import com.salma.mini_projet_pharmacie.repository.FournitureRepository;
import com.salma.mini_projet_pharmacie.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FournitureService {

    private final FournitureRepository fournitureRepository;
    private final ProduitRepository produitRepository;
    private final FournisseurRepository fournisseurRepository;

    public FournitureDTO ajouterFourniture(Integer idProduit, Integer idFournisseur, Double prixAchat) {

        Produit produit = produitRepository.findById(idProduit)
                .orElseThrow(() -> new NotFoundException("Produit introuvable"));

        Fournisseur fournisseur = fournisseurRepository.findById(idFournisseur)
                .orElseThrow(() -> new NotFoundException("Fournisseur introuvable"));

        Fourniture fourniture = new Fourniture(
                new FournitureKey(idProduit, idFournisseur),
                produit,
                fournisseur,
                prixAchat
        );

        Fourniture saved = fournitureRepository.save(fourniture);
        return FournitureMapper.toDTO(saved);
    }

    //  LISTE par fournisseur
    public List<FournitureDTO> listeParFournisseur(Integer idFournisseur) {
        return fournitureRepository.findByFournisseur_IdFournisseur(idFournisseur)
                .stream()
                .map(FournitureMapper::toDTO)
                .toList();
    }

    //  MODIFIER prix achat
    public FournitureDTO modifierPrix(Integer idProduit, Integer idFournisseur, Double prixAchat) {
        FournitureKey key = new FournitureKey(idProduit, idFournisseur);

        Fourniture f = fournitureRepository.findById(key)
                .orElseThrow(() -> new NotFoundException("Fourniture introuvable"));

        f.setPrixAchat(prixAchat);
        return FournitureMapper.toDTO(fournitureRepository.save(f));
    }

    //  SUPPRIMER lien
    public void supprimer(Integer idProduit, Integer idFournisseur) {
        FournitureKey key = new FournitureKey(idProduit, idFournisseur);
        fournitureRepository.deleteById(key);
    }
}
