package com.salma.mini_projet_pharmacie.repository;

import com.salma.mini_projet_pharmacie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
}
