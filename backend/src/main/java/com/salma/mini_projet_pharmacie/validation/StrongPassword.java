package com.salma.mini_projet_pharmacie.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Politique de mot de passe appliquee a toute creation ou modification de mot de
 * passe (inscription client, creation/modification d'un pharmacien). Jamais a la
 * connexion : les comptes existants restent utilisables.
 *
 * <p>8 a 72 caracteres (72 = limite de BCrypt), au moins une minuscule, une
 * majuscule et un chiffre. {@code null} est considere valide : combiner avec
 * {@code @NotBlank} quand le champ est obligatoire.
 *
 * <p>La meme regex est reprise cote frontend (validationSchemas.ts).
 */
@Documented
@Pattern(regexp = StrongPassword.REGEX)
@ReportAsSingleViolation
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

    String REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,72}$";

    String MESSAGE = "Le mot de passe doit contenir au moins 8 caractères, dont une majuscule,"
            + " une minuscule et un chiffre.";

    String message() default MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
