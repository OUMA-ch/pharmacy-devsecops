package com.salma.mini_projet_pharmacie.service;

import com.salma.mini_projet_pharmacie.dto.LoginRequestDTO;
import com.salma.mini_projet_pharmacie.dto.UserResponseDTO;
import com.salma.mini_projet_pharmacie.exception.BadCredentialsException;
import com.salma.mini_projet_pharmacie.exception.UserNotFoundException;
import com.salma.mini_projet_pharmacie.mapper.UserMapper;
import com.salma.mini_projet_pharmacie.model.User;
import com.salma.mini_projet_pharmacie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponseDTO login(LoginRequestDTO request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UserNotFoundException("Utilisateur introuvable")
                );

        if (!user.getPassword().equals(request.getPassword())) {
            throw new BadCredentialsException("Mot de passe incorrect");
        }

        return userMapper.toDto(user);
    }
}
