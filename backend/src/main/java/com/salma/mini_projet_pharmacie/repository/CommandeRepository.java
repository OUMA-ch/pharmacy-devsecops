package com.salma.mini_projet_pharmacie.repository;

import com.salma.mini_projet_pharmacie.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Integer> {

    @Query("""
        select distinct c
        from Commande c
        left join fetch c.lignes l
        left join fetch l.produit
        left join fetch c.fournisseur
    """)
    List<Commande> findAllWithDetails();
}
