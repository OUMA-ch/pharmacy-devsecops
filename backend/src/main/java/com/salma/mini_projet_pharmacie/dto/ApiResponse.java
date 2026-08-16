package com.salma.mini_projet_pharmacie.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {
    private String status;   // SUCCESS / ERROR
    private String message;  // Message détaillé
    private T data;          // Le contenu (DTO)
}