package com.salma.mini_projet_pharmacie.controller;

import com.salma.mini_projet_pharmacie.model.Client;
import com.salma.mini_projet_pharmacie.service.UserService;
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

    @PostMapping("/register")
    public Client register(@RequestBody Client client) {
        return userService.registerClient(client);
    }
}
