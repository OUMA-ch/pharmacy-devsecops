package com.salma.mini_projet_pharmacie.repository;

import com.salma.mini_projet_pharmacie.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Integer> {

    // Produits avec stock faible (< 10)
    List<Produit> findByQuantiteStockLessThan(int stock);

    // Produits périmés
    List<Produit> findByDatePeremptionBefore(Date date);
}