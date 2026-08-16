package com.salma.mini_projet_pharmacie.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
@PrimaryKeyJoinColumn(name="idUser")
@Entity
@Table(name = "client")
public class Client extends User {

    // Constructeur vide requis par JPA
    public Client() {
        super();
    }
}
