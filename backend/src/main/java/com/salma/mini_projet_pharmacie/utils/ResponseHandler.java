package com.salma.mini_projet_pharmacie.utils;

import com.salma.mini_projet_pharmacie.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHandler {

    public static <T> ResponseEntity<ApiResponse<?>> success(String message, T data) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .status("SUCCESS")
                .message(message)
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<ApiResponse<?>> error(String message, HttpStatus status) {
        ApiResponse<?> response = ApiResponse.builder()
                .status("ERROR")
                .message(message)
                .data(null)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
