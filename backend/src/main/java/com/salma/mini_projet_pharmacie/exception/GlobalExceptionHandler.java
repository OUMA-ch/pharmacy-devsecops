package com.salma.mini_projet_pharmacie.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

    // Levee par les @PreAuthorize (ex: verification IDOR sur /notifications/client/{id}).
    // Spring resout ce handler avant le handleRuntime generique ci-dessous car
    // AccessDeniedException est le type le plus specifique (Spring choisit toujours
    // le @ExceptionHandler le plus proche dans la hierarchie, peu importe l'ordre
    // de declaration) : sans lui, une verification de role echouee remonterait en
    // 400 au lieu de 403.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Acces refuse pour ce role."));
    }


        // Corps de requete illisible (ex: date "01/09/2026" au lieu du format ISO
        // attendu par LocalDate) : sans ce handler, HttpMessageNotReadableException
        // (sous-classe de RuntimeException) tombait dans handleRuntime ci-dessous et
        // exposait le message technique brut de Jackson/DateTimeParseException au
        // client au lieu d'un message exploitable (cf. README-SECURITY.md, jamais de
        // trace technique brute affichee a l'utilisateur).
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<Map<String, String>> handleUnreadable(HttpMessageNotReadableException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Donnee invalide : verifiez le format des champs (dates au format AAAA-MM-JJ, nombres)."));
        }

        @ExceptionHandler(StockInsuffisantException.class)
        public ResponseEntity<Map<String, String>> handleStock(StockInsuffisantException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
}
