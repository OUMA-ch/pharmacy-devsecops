package com.salma.mini_projet_pharmacie.security;

import com.salma.mini_projet_pharmacie.model.User;
import com.salma.mini_projet_pharmacie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Migre au demarrage tout mot de passe encore stocke en clair vers BCrypt.
 * Le mot de passe existant est repris tel quel puis hache (pas de reset
 * force) : l'utilisateur peut continuer a se connecter avec le meme mot de
 * passe, seul son stockage change. Idempotent : un mot de passe deja au
 * format BCrypt est ignore, donc sans effet lors des demarrages suivants.
 */
@Component
@RequiredArgsConstructor
public class PasswordMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PasswordMigrationRunner.class);
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        List<User> users = userRepository.findAll();
        int migrated = 0;

        for (User user : users) {
            String currentPassword = user.getPassword();
            if (currentPassword != null && !BCRYPT_PATTERN.matcher(currentPassword).matches()) {
                user.setPassword(passwordEncoder.encode(currentPassword));
                userRepository.save(user);
                migrated++;
            }
        }

        if (migrated > 0) {
            log.info("Migration mots de passe : {} compte(s) migre(s) vers BCrypt.", migrated);
        }
    }
}
