package com.salma.mini_projet_pharmacie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class WebConfig {

    // Liste explicite (pas de "*") : obligatoire pour que le navigateur envoie le
    // cookie HttpOnly d'authentification cross-origin (allowCredentials(true) est
    // incompatible avec allowedOrigins("*"), refuse par les navigateurs). A adapter
    // en production via la variable d'environnement CORS_ALLOWED_ORIGINS.
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8080,http://localhost:3000}")
    private String[] allowedOrigins;

    // Bean branche explicitement dans SecurityConfig via .cors(...) : le CorsFilter
    // de Spring Security s'execute alors avant les filtres d'authentification et
    // ajoute les en-tetes CORS meme sur les reponses d'erreur (401/403) ecrites
    // directement par RestAuthEntryPoints. Avec uniquement une config MVC
    // (WebMvcConfigurer.addCorsMappings), ces reponses d'erreur partaient sans
    // Access-Control-Allow-Origin et le navigateur les affichait comme un blocage
    // CORS, masquant le vrai 401/403.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
