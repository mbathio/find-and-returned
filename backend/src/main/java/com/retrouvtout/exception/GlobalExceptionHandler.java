package com.retrouvtout.exception;

import com.retrouvtout.dto.response.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * ✅ GESTIONNAIRE GLOBAL DES EXCEPTIONS AVEC DEBUG AVANCÉ
 * Logs détaillés pour identifier l'origine de l'erreur 500
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ✅ Gestion des erreurs de validation des données JSON
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        System.err.println("❌ ERREUR JSON: " + ex.getMessage());
        ex.printStackTrace();
        
        ApiResponse<Object> response = new ApiResponse<>(
            false, 
            "Données JSON invalides ou malformées", 
            null
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * ✅ Gestion des erreurs de contrainte de base de données
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {
        
        System.err.println("❌ ERREUR BASE DE DONNÉES: " + ex.getMessage());
        ex.printStackTrace();
        
        String message = "Erreur de données";
        
        // Analyser le message d'erreur pour donner des détails utiles
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("email")) {
                message = "Cette adresse email est déjà utilisée";
            } else if (ex.getMessage().contains("phone")) {
                message = "Ce numéro de téléphone est déjà utilisé";
            } else if (ex.getMessage().contains("Duplicate entry")) {
                message = "Ces informations sont déjà utilisées par un autre utilisateur";
            }
        }
        
        ApiResponse<Object> response = new ApiResponse<>(false, message, null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * ✅ GESTION AMÉLIORÉE DES ERREURS DE VALIDATION
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        System.err.println("❌ ERREUR DE VALIDATION: " + ex.getMessage());
        ex.printStackTrace();
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            System.err.println("  - Champ '" + fieldName + "': " + errorMessage);
        });

        // Message principal basé sur la première erreur
        String mainMessage = "Erreurs de validation";
        if (!errors.isEmpty()) {
            mainMessage = errors.values().iterator().next();
        }

        ApiResponse<Object> response = new ApiResponse<>(false, mainMessage, errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gestion des ressources non trouvées
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        System.err.println("❌ RESSOURCE NON TROUVÉE: " + ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Gestion des requêtes malformées
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequestException(
            BadRequestException ex, WebRequest request) {
        
        System.err.println("❌ REQUÊTE MALFORMÉE: " + ex.getMessage());
        ex.printStackTrace();
        
        ApiResponse<Object> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gestion des erreurs d'authentification
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        
        System.err.println("❌ ERREUR D'AUTHENTIFICATION: " + ex.getMessage());
        ex.printStackTrace();
        
        String message = ex instanceof BadCredentialsException ? 
            "Identifiants invalides" : "Erreur d'authentification";
        
        ApiResponse<Object> response = new ApiResponse<>(false, message, null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Gestion des erreurs d'autorisation
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Object>> handleSecurityException(
            SecurityException ex, WebRequest request) {
        
        System.err.println("❌ ERREUR DE SÉCURITÉ: " + ex.getMessage());
        ex.printStackTrace();
        
        ApiResponse<Object> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * ✅ GESTION AMÉLIORÉE DES ARGUMENTS ILLÉGAUX
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        System.err.println("❌ ARGUMENT ILLÉGAL: " + ex.getMessage());
        ex.printStackTrace();
        
        ApiResponse<Object> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gestion des états illégaux
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {
        
        System.err.println("❌ ÉTAT ILLÉGAL: " + ex.getMessage());
        ex.printStackTrace();
        
        ApiResponse<Object> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * ✅ GESTION AMÉLIORÉE DES RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        System.err.println("❌ RUNTIME EXCEPTION: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        ex.printStackTrace();
        
        // Log de la stack trace complète pour debug
        System.err.println("📍 Stack trace complète:");
        for (StackTraceElement element : ex.getStackTrace()) {
            if (element.getClassName().contains("retrouvtout")) {
                System.err.println("  at " + element.toString());
            }
        }
        
        ApiResponse<Object> response = new ApiResponse<>(
            false, 
            ex.getMessage() != null ? ex.getMessage() : "Une erreur interne s'est produite", 
            null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * ✅ GESTION DE TOUTES LES AUTRES EXCEPTIONS AVEC DEBUG MAXIMUM
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        System.err.println("❌ EXCEPTION NON GÉRÉE: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        System.err.println("📍 URL de la requête: " + request.getDescription(false));
        
        // Log de la stack trace complète
        System.err.println("📍 Stack trace complète:");
        ex.printStackTrace();
        
        // Log des causes imbriquées
        Throwable cause = ex.getCause();
        int level = 1;
        while (cause != null && level <= 3) {
            System.err.println("📍 Cause niveau " + level + ": " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
            cause = cause.getCause();
            level++;
        }
        
        ApiResponse<Object> response = new ApiResponse<>(
            false, 
            "Une erreur interne s'est produite. Veuillez réessayer.", 
            null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}