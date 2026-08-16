package com.salma.mini_projet_pharmacie.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.*;
@PrimaryKeyJoinColumn(name="idUser")
@Entity
@Table(name = "responsable")
@Getter
@Setter
@NoArgsConstructor
public class Responsable extends User {
}
