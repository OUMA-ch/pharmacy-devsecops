package com.salma.mini_projet_pharmacie.repository;

import com.salma.mini_projet_pharmacie.model.Client;
import com.salma.mini_projet_pharmacie.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByClientOrderByDateEnvoiDesc(Client client);

    List<Notification> findByClientAndMessageContainingIgnoreCase(
            Client client, String keyword
    );
}
