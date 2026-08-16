package com.salma.mini_projet_pharmacie.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {
    private Integer id;
    private String nom;
    private String email;
    private String role;
}
