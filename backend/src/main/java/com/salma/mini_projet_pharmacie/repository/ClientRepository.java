package com.salma.mini_projet_pharmacie.repository;

import com.salma.mini_projet_pharmacie.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Integer> {
}
