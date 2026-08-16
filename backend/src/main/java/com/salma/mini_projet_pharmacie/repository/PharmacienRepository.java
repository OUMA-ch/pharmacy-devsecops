package com.salma.mini_projet_pharmacie.repository;

import com.salma.mini_projet_pharmacie.model.Pharmacien;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacienRepository extends JpaRepository<Pharmacien, Integer> {
}
