package com.salma.mini_projet_pharmacie.repository;

import com.salma.mini_projet_pharmacie.model.Vente;
import com.salma.mini_projet_pharmacie.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenteRepository extends JpaRepository<Vente,Integer> {

    // Récupérer toutes les ventes d’un client (historique)
    List<Vente> findByClient(Client client);
    boolean existsByProduit_IdProduit(Integer idProduit);
}