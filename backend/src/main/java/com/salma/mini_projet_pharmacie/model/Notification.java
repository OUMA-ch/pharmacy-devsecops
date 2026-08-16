package com.salma.mini_projet_pharmacie.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String titre;

    @Column(length = 1000)
    private String message;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;


    private boolean lu = false;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    // ===== getters & setters =====
    public Integer getId() { return id; }
    public String getTitre() { return titre; }
    public String getMessage() { return message; }
    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public boolean isLu() { return lu; }
    public Client getClient() { return client; }

    public void setId(Integer id) { this.id = id; }
    public void setTitre(String titre) { this.titre = titre; }
    public void setMessage(String message) { this.message = message; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }
    public void setLu(boolean lu) { this.lu = lu; }
    public void setClient(Client client) { this.client = client; }
}
