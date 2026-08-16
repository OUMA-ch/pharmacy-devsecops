package com.salma.mini_projet_pharmacie.repository;

import com.salma.mini_projet_pharmacie.model.Fourniture;
import com.salma.mini_projet_pharmacie.model.FournitureKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FournitureRepository extends JpaRepository<Fourniture, FournitureKey> {

    List<Fourniture> findByFournisseur_IdFournisseur(Integer idFournisseur);

    List<Fourniture> findByProduit_IdProduit(Integer idProduit);
}
