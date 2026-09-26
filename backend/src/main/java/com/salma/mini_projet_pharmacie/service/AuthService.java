package com.salma.mini_projet_pharmacie.service;

import com.salma.mini_projet_pharmacie.dto.LoginRequestDTO;
import com.salma.mini_projet_pharmacie.dto.UserResponseDTO;
import com.salma.mini_projet_pharmacie.exception.BadCredentialsException;
import com.salma.mini_projet_pharmacie.exception.TooManyLoginAttemptsException;
import com.salma.mini_projet_pharmacie.mapper.UserMapper;
import com.salma.mini_projet_pharmacie.model.User;
import com.salma.mini_projet_pharmacie.repository.UserRepository;
import com.salma.mini_projet_pharmacie.security.LoginAttemptService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    // Message unique pour email inconnu ET mauvais mot de passe : ne revele pas
    // si un compte existe pour cet email (enumeration d'utilisateurs).
    public static final String BAD_CREDENTIALS_MESSAGE = "Email ou mot de passe incorrect.";
    public static final String TOO_MANY_ATTEMPTS_MESSAGE =
            "Trop de tentatives de connexion. Réessayez dans une minute.";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    // Hash BCrypt factice compare quand l'email est inconnu : le temps de reponse
    // reste le meme que pour un mauvais mot de passe (pas d'enumeration par le temps).
    private final String dummyPasswordHash;

    public AuthService(UserRepository userRepository, UserMapper userMapper,
                       PasswordEncoder passwordEncoder, LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    public UserResponseDTO login(LoginRequestDTO request, String clientIp) {
        // Le compteur utilise l'email normalise (LoginAttemptService) ; la recherche
        // en base garde l'email saisi, comme avant, pour ne rien changer aux comptes existants.
        String email = request.getEmail() == null ? "" : request.getEmail();
        String rawPassword = request.getPassword() == null ? "" : request.getPassword();

        if (loginAttemptService.isBlocked(email, clientIp)) {
            throw new TooManyLoginAttemptsException(TOO_MANY_ATTEMPTS_MESSAGE);
        }

        Optional<User> user = userRepository.findByEmail(email);
        String storedHash = user.map(User::getPassword).orElse(dummyPasswordHash);
        boolean passwordMatches = passwordEncoder.matches(rawPassword, storedHash);

        if (user.isEmpty() || !passwordMatches) {
            loginAttemptService.recordFailure(email, clientIp);
            throw new BadCredentialsException(BAD_CREDENTIALS_MESSAGE);
        }

        loginAttemptService.resetEmail(email);
        return userMapper.toDto(user.get());
    }
}
