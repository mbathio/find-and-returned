package com.retrouvtout.controller;

import com.retrouvtout.dto.request.LoginRequest;
import com.retrouvtout.dto.request.RegisterRequest;
import com.retrouvtout.dto.request.RefreshTokenRequest;
import com.retrouvtout.dto.response.AuthResponse;
import com.retrouvtout.dto.response.ApiResponse;
import com.retrouvtout.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ✅ CONTRÔLEUR D'AUTHENTIFICATION CORRIGÉ
 * Suppression des validations trop strictes causant l'erreur 500
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
     * ✅ CONNEXION SIMPLIFIÉE pour éviter l'erreur 500
     */
    @PostMapping("/login")
    @Operation(summary = "Connexion d'un utilisateur")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        try {
            System.out.println("🚀 Tentative de connexion pour: " + loginRequest.getEmail());
            
            // ✅ Validation basique uniquement
            if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "L'email est obligatoire", null));
            }
            
            if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Le mot de passe est obligatoire", null));
            }

            // Appel du service d'authentification
            AuthResponse authResponse = authService.login(loginRequest, getClientIp(request));
            
            System.out.println("✅ Connexion réussie pour: " + loginRequest.getEmail());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Connexion réussie", authResponse));
            
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur de validation: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, e.getMessage(), null));
                
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
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
     * ✅ INSCRIPTION SIMPLIFIÉE
     */
    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request) {
        
        try {
            System.out.println("🚀 Tentative d'inscription pour: " + registerRequest.getEmail());
            
            // ✅ Validation basique uniquement
            if (registerRequest.getName() == null || registerRequest.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Le nom est obligatoire", null));
            }
            
            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "L'email est obligatoire", null));
            }
            
            if (registerRequest.getPassword() == null || registerRequest.getPassword().length() < 6) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Le mot de passe doit contenir au moins 6 caractères", null));
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
     * ✅ RAFRAÎCHISSEMENT DE TOKEN
     */
    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchissement du token d'accès")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        
        try {
            if (refreshTokenRequest.getRefreshToken() == null || refreshTokenRequest.getRefreshToken().trim().isEmpty()) {
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
     * ✅ ENDPOINT DE TEST
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