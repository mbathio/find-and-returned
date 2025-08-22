package com.retrouvtout.controller;

import com.retrouvtout.dto.request.LoginRequest;
import com.retrouvtout.dto.request.RegisterRequest;
import com.retrouvtout.dto.request.RefreshTokenRequest;
import com.retrouvtout.dto.response.AuthResponse;
import com.retrouvtout.dto.response.ApiResponse;
import com.retrouvtout.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ✅ CONTRÔLEUR D'AUTHENTIFICATION COMPLET ET ROBUSTE
 * Gestion complète des erreurs avec messages détaillés
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API d'authentification")
@CrossOrigin(origins = {"*"}, maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * ✅ INSCRIPTION ROBUSTE avec validation complète
     */
    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            BindingResult bindingResult,
            HttpServletRequest request) {
        
        try {
            // Log pour debug
            System.out.println("🚀 Tentative d'inscription pour: " + registerRequest.getEmail());
            
            // Vérification des erreurs de validation
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error -> 
                    errors.put(error.getField(), error.getDefaultMessage())
                );
                
                System.out.println("❌ Erreurs de validation: " + errors);
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Données invalides", null));
            }

            // Validation manuelle supplémentaire
            String validationError = validateRegisterRequest(registerRequest);
            if (validationError != null) {
                System.out.println("❌ Erreur de validation manuelle: " + validationError);
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, validationError, null));
            }

            // Appel du service d'authentification
            AuthResponse authResponse = authService.register(registerRequest, getClientIp(request));
            
            System.out.println("✅ Inscription réussie pour: " + registerRequest.getEmail());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Inscription réussie", authResponse));
                
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur métier lors de l'inscription: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, e.getMessage(), null));
                
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de l'inscription: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erreur interne du serveur. Veuillez réessayer.", null));
        }
    }

    /**
     * ✅ CONNEXION ROBUSTE avec gestion d'erreur complète
     */
    @PostMapping("/login")
    @Operation(summary = "Connexion d'un utilisateur")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            BindingResult bindingResult,
            HttpServletRequest request) {
        
        try {
            System.out.println("🚀 Tentative de connexion pour: " + loginRequest.getEmail());
            
            // Vérification des erreurs de validation
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error -> 
                    errors.put(error.getField(), error.getDefaultMessage())
                );
                
                System.out.println("❌ Erreurs de validation: " + errors);
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Données invalides", null));
            }

            // Validation manuelle
            String validationError = validateLoginRequest(loginRequest);
            if (validationError != null) {
                System.out.println("❌ Erreur de validation manuelle: " + validationError);
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, validationError, null));
            }

            // Appel du service
            AuthResponse authResponse = authService.login(loginRequest, getClientIp(request));
            
            System.out.println("✅ Connexion réussie pour: " + loginRequest.getEmail());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Connexion réussie", authResponse));
            
        } catch (BadCredentialsException e) {
            System.out.println("❌ Identifiants incorrects pour: " + loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, "Email ou mot de passe incorrect", null));
                
        } catch (IllegalStateException e) {
            System.out.println("❌ Compte désactivé pour: " + loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, e.getMessage(), null));
                
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de la connexion: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erreur interne du serveur. Veuillez réessayer.", null));
        }
    }

    /**
     * ✅ RAFRAÎCHISSEMENT DE TOKEN
     */
    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchissement du token d'accès")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
            BindingResult bindingResult) {
        
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Token de rafraîchissement manquant", null));
            }

            AuthResponse authResponse = authService.refreshToken(refreshTokenRequest.getRefreshToken());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Token rafraîchi avec succès", authResponse));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, "Token de rafraîchissement invalide", null));
                
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du refresh: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erreur lors du rafraîchissement", null));
        }
    }

    /**
     * ✅ DÉCONNEXION
     */
    @PostMapping("/logout")
    @Operation(summary = "Déconnexion de l'utilisateur")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                authService.logout(token);
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Déconnexion réussie", null));
            
        } catch (Exception e) {
            // La déconnexion ne doit jamais échouer
            return ResponseEntity.ok(new ApiResponse<>(true, "Déconnexion réussie", null));
        }
    }

    /**
     * ✅ ENDPOINT DE TEST POUR L'AUTHENTIFICATION
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testAuth() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Endpoints d'authentification disponibles");
        response.put("timestamp", System.currentTimeMillis());
        response.put("endpoints", new String[]{
            "POST /api/auth/register",
            "POST /api/auth/login", 
            "POST /api/auth/refresh",
            "POST /api/auth/logout"
        });
        
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ OAUTH2 DÉSACTIVÉ EN DÉVELOPPEMENT
     */
    @GetMapping("/oauth2/google")
    public ResponseEntity<ApiResponse<String>> googleOAuth2() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ApiResponse<>(false, "OAuth2 Google non configuré en développement", null));
    }

    @GetMapping("/oauth2/facebook")
    public ResponseEntity<ApiResponse<String>> facebookOAuth2() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ApiResponse<>(false, "OAuth2 Facebook non configuré en développement", null));
    }

    // ✅ MÉTHODES UTILITAIRES PRIVÉES

    /**
     * Validation manuelle des données d'inscription
     */
    private String validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            return "Données d'inscription manquantes";
        }
        
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return "Le nom est obligatoire";
        }
        
        if (request.getName().trim().length() > 120) {
            return "Le nom ne peut pas dépasser 120 caractères";
        }
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return "L'email est obligatoire";
        }
        
        if (!isValidEmail(request.getEmail())) {
            return "Format d'email invalide";
        }
        
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return "Le mot de passe doit contenir au moins 6 caractères";
        }
        
        if (request.getPassword().length() > 255) {
            return "Le mot de passe ne peut pas dépasser 255 caractères";
        }
        
        // Validation du téléphone si fourni
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (request.getPhone().trim().length() > 40) {
                return "Le numéro de téléphone ne peut pas dépasser 40 caractères";
            }
        }
        
        // Validation du rôle si fourni
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            String role = request.getRole().trim().toLowerCase();
            if (!role.equals("retrouveur") && !role.equals("proprietaire") && !role.equals("mixte")) {
                return "Rôle invalide. Valeurs acceptées: retrouveur, proprietaire, mixte";
            }
        }
        
        return null; // Aucune erreur
    }

    /**
     * Validation manuelle des données de connexion
     */
    private String validateLoginRequest(LoginRequest request) {
        if (request == null) {
            return "Données de connexion manquantes";
        }
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return "L'email est obligatoire";
        }
        
        if (!isValidEmail(request.getEmail())) {
            return "Format d'email invalide";
        }
        
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return "Le mot de passe est obligatoire";
        }
        
        return null; // Aucune erreur
    }

    /**
     * Validation du format email
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.trim().matches(emailRegex);
    }

    /**
     * Obtenir l'adresse IP du client
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}