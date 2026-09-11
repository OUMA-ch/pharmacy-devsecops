package com.salma.mini_projet_pharmacie.controller;

import com.salma.mini_projet_pharmacie.dto.LoginRequestDTO;
import com.salma.mini_projet_pharmacie.dto.UserResponseDTO;
import com.salma.mini_projet_pharmacie.security.JwtService;
import com.salma.mini_projet_pharmacie.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String COOKIE_NAME = "access_token";

    private final AuthService authService;
    private final JwtService jwtService;

    // true par defaut (production HTTPS). A mettre a false uniquement pour tester en
    // HTTP simple hors du cas particulier "localhost" que certains navigateurs
    // exemptent deja de la contrainte Secure (voir README-SECURITY.md).
    @Value("${security.cookie.secure:true}")
    private boolean cookieSecure;

    // "Strict" par defaut (dev, backend/frontend sur le meme "site" au sens du
    // navigateur, ex. localhost:5173 -> localhost:8080). En production, quand le
    // frontend et le backend sont sur des sous-domaines differents d'un domaine
    // present dans la Public Suffix List (ex. deux services *.onrender.com), le
    // navigateur les traite comme des sites distincts : SameSite=Strict (et meme
    // Lax) empeche alors le cookie de voyager sur les appels cross-site du
    // frontend, meme apres une connexion reussie -> 401 partout. Il faut passer
    // a "None" via SECURITY_COOKIE_SAMESITE, ce qui exige security.cookie.secure=true
    // (deja le cas par defaut, obligatoire avec SameSite=None).
    @Value("${security.cookie.same-site:Strict}")
    private String cookieSameSite;

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody LoginRequestDTO request) {
        UserResponseDTO user = authService.login(request);

        String token = jwtService.generateToken(user.getEmail(), user.getRole(), user.getId());

        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(Duration.ofMillis(jwtService.expirationMs()))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
