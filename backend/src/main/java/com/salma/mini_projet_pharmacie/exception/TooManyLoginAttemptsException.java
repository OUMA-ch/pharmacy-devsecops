package com.salma.mini_projet_pharmacie.exception;

/** Trop d'echecs de connexion recents pour cet email ou cette IP (429, voir GlobalExceptionHandler). */
public class TooManyLoginAttemptsException extends RuntimeException {
    public TooManyLoginAttemptsException(String message) {
        super(message);
    }
}
