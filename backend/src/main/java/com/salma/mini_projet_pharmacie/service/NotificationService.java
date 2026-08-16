package com.salma.mini_projet_pharmacie.service;

import com.salma.mini_projet_pharmacie.model.Client;
import com.salma.mini_projet_pharmacie.model.Notification;
import com.salma.mini_projet_pharmacie.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // 🔔 créer notification
    public void notifierClient(Client client, String titre, String message) {
        Notification n = new Notification();
        n.setClient(client);
        n.setTitre(titre);
        n.setMessage(message);
        n.setDateEnvoi(LocalDateTime.now());
        n.setLu(false);

        notificationRepository.save(n);
    }

    // 📥 liste notifications client
    public List<Notification> notificationsClient(Integer clientId) {
        Client c = new Client();
        c.setId(clientId);
        return notificationRepository.findByClientOrderByDateEnvoiDesc(c);
    }

    // 🔍 recherche
    public List<Notification> rechercher(Integer clientId, String keyword) {
        Client c = new Client();
        c.setId(clientId);
        return notificationRepository
                .findByClientAndMessageContainingIgnoreCase(c, keyword);
    }
}
