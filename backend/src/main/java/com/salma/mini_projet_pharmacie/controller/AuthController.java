package com.salma.mini_projet_pharmacie.controller;

import com.salma.mini_projet_pharmacie.dto.LoginRequestDTO;
import com.salma.mini_projet_pharmacie.dto.UserResponseDTO;
import com.salma.mini_projet_pharmacie.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public UserResponseDTO login(@RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }
}
