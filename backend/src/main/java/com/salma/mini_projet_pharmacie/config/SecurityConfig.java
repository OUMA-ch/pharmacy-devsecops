package com.salma.mini_projet_pharmacie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())   // désactiver CSRF
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // tout autoriser
                )
                .formLogin(form -> form.disable())   // pas d'authentification
                .httpBasic(basic -> basic.disable()); // pas de basic auth

        return http.build();
    }
}
