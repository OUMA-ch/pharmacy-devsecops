package com.salma.mini_projet_pharmacie.controller;

import com.salma.mini_projet_pharmacie.dto.RegisterClientDTO;
import com.salma.mini_projet_pharmacie.dto.UserResponseDTO;
import com.salma.mini_projet_pharmacie.mapper.UserMapper;
import com.salma.mini_projet_pharmacie.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public UserResponseDTO register(@Valid @RequestBody RegisterClientDTO dto) {
        return userMapper.toDto(userService.registerClient(dto));
    }
}