package com.salma.mini_projet_pharmacie.repository;

import com.salma.mini_projet_pharmacie.model.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Integer> {
}
