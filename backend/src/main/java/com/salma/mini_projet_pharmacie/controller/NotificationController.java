package com.salma.mini_projet_pharmacie.controller;

import com.salma.mini_projet_pharmacie.model.Notification;
import com.salma.mini_projet_pharmacie.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/client/{id}")
    public List<Notification> list(@PathVariable Integer id) {
        return notificationService.notificationsClient(id);
    }

    @GetMapping("/client/{id}/search")
    public List<Notification> search(
            @PathVariable Integer id,
            @RequestParam String q
    ) {
        return notificationService.rechercher(id, q);
    }
}
