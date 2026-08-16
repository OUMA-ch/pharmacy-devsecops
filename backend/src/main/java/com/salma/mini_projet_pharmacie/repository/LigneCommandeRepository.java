package com.salma.mini_projet_pharmacie.repository;

import com.salma.mini_projet_pharmacie.model.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Integer> {
    boolean existsByProduit_IdProduit(Integer idProduit);
}
