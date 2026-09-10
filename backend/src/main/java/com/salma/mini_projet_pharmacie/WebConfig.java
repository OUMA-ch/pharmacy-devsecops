package com.salma.mini_projet_pharmacie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    // Liste explicite (pas de "*") : obligatoire pour que le navigateur envoie le
    // cookie HttpOnly d'authentification cross-origin (allowCredentials(true) est
    // incompatible avec allowedOrigins("*"), refuse par les navigateurs). A adapter
    // en production via la variable d'environnement CORS_ALLOWED_ORIGINS.
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8080,http://localhost:3000}")
    private String[] allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Aucun controleur de ce projet n'utilise le prefixe "/api" (voir ProduitController: "/produits"),
                // le mapping CORS couvre donc toutes les routes exposees par l'application.
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowCredentials(true);
            }
        };
    }
}
