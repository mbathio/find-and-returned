package com.retrouvtout.controller;

import com.retrouvtout.dto.request.LoginRequest;
import com.retrouvtout.dto.request.RegisterRequest;
import com.retrouvtout.dto.request.RefreshTokenRequest;
import com.retrouvtout.dto.response.AuthResponse;
import com.retrouvtout.dto.response.ApiResponse;
import com.retrouvtout.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Contrôleur pour l'authentification
 * Conforme au cahier des charges - Section 3.1
 * Inscription/Connexion via email ou réseaux sociaux
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API d'authentification - Email et réseaux sociaux")
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Inscription - Cahier des charges 3.1
     * Créer un compte via email
     */
    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Données invalides"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request) {
        
        try {
            AuthResponse authResponse = authService.register(registerRequest, getClientIp(request));
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                    true,
                    "Inscription réussie",
                    authResponse
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null
                ));
        }
    }

    /**
     * Connexion - Cahier des charges 3.1
     * Se connecter via email
     */
    @PostMapping("/login")
    @Operation(summary = "Connexion d'un utilisateur")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Connexion réussie"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Identifiants invalides"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Compte désactivé")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        try {
            AuthResponse authResponse = authService.login(loginRequest, getClientIp(request));
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Connexion réussie",
                authResponse
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                    false,
                    "Email ou mot de passe incorrect",
                    null
                ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null
                ));
        }
    }

    /**
     * Rafraîchissement du token d'accès
     */
    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchissement du token d'accès")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token rafraîchi avec succès"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token de rafraîchissement invalide")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        
        try {
            AuthResponse authResponse = authService.refreshToken(refreshTokenRequest.getRefreshToken());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Token rafraîchi avec succès",
                authResponse
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                    false,
                    "Token de rafraîchissement invalide",
                    null
                ));
        }
    }

    /**
     * Déconnexion
     */
    @PostMapping("/logout")
    @Operation(summary = "Déconnexion de l'utilisateur")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Déconnexion réussie")
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            String token = authHeader.startsWith("Bearer ") ? 
                authHeader.substring(7) : authHeader;
            
            authService.logout(token);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Déconnexion réussie",
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Déconnexion réussie",
                null
            ));
        }
    }

    /**
     * Authentification OAuth2 Google - Cahier des charges 3.1
     * Connexion via réseaux sociaux
     */
    @GetMapping("/oauth2/google")
    @Operation(summary = "Redirection vers l'authentification Google")
    public void googleOAuth2(HttpServletResponse response) throws IOException {
        String googleAuthUrl = authService.getGoogleAuthUrl();
        response.sendRedirect(googleAuthUrl);
    }

    /**
     * Authentification OAuth2 Facebook - Cahier des charges 3.1
     * Connexion via réseaux sociaux
     */
    @GetMapping("/oauth2/facebook")
    @Operation(summary = "Redirection vers l'authentification Facebook")
    public void facebookOAuth2(HttpServletResponse response) throws IOException {
        String facebookAuthUrl = authService.getFacebookAuthUrl();
        response.sendRedirect(facebookAuthUrl);
    }

    /**
     * Callback pour l'authentification OAuth2
     */
    @GetMapping("/oauth2/callback/{provider}")
    @Operation(summary = "Callback OAuth2")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Authentification OAuth2 réussie"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Erreur d'authentification OAuth2")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> oauth2Callback(
            @Parameter(description = "Fournisseur OAuth2 (google, facebook)")
            @PathVariable String provider,
            @RequestParam String code,
            @RequestParam(required = false) String state,
            HttpServletRequest request) {
        
        try {
            AuthResponse authResponse = authService.processOAuth2Callback(
                provider, code, state, getClientIp(request)
            );
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Authentification OAuth2 réussie",
                authResponse
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(
                    false,
                    "Erreur d'authentification OAuth2: " + e.getMessage(),
                    null
                ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    false,
                    "Erreur interne lors de l'authentification OAuth2",
                    null
                ));
        }
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