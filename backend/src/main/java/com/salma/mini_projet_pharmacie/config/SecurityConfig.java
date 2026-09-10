package com.salma.mini_projet_pharmacie.config;

import com.salma.mini_projet_pharmacie.security.JwtAuthFilter;
import com.salma.mini_projet_pharmacie.security.RestAuthEntryPoints;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

/**
 * RBAC applique cote serveur — seule source de verite pour l'autorisation
 * (voir README-SECURITY.md). Le frontend peut masquer des liens pour l'UX,
 * mais chaque requete est ici verifiee independamment de ce que l'UI affiche.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /** Force de calcul par defaut (10) : compromis eprouve cout CPU / resistance au brute-force. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Stateless + cookie SameSite=Strict : le risque CSRF classique (formulaire
                // HTML tiers) ne s'applique pas ici. A revisiter si un flux avec effets de
                // bord via GET apparaissait (voir README-SECURITY.md, "points ouverts").
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(RestAuthEntryPoints.unauthorized())
                        .accessDeniedHandler(RestAuthEntryPoints.forbidden())
                )
                .authorizeHttpRequests(auth -> auth
                        // Les requetes de pre-verification CORS (OPTIONS) ne portent jamais de
                        // cookie/credential : elles doivent passer avant toute verification de
                        // role, sinon le navigateur bloque l'appel reel avant meme qu'il parte
                        // (le CorsRegistry de WebConfig ne s'execute qu'apres ce filtre).
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/auth/login", "/auth/logout", "/users/register").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/pharmaciens/**", "/reports/**").hasRole("RESPONSABLE")
                        .requestMatchers(
                                "/produits/**", "/ventes/**", "/commandes/**",
                                "/fournisseurs/**", "/fournitures/**", "/ordonnances/**"
                        ).hasAnyRole("PHARMACIEN", "RESPONSABLE")
                        .requestMatchers("/notifications/**").hasAnyRole("CLIENT", "PHARMACIEN", "RESPONSABLE")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
