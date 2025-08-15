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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
 * Contrôleur pour l'authentification et l'autorisation
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API d'authentification et d'autorisation")
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Inscription d'un nouvel utilisateur
     */
    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur")
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "201", description = "Utilisateur créé avec succès"),
        @SwaggerApiResponse(responseCode = "400", description = "Données invalides"),
        @SwaggerApiResponse(responseCode = "409", description = "Email déjà utilisé")
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
     * Connexion d'un utilisateur
     */
    @PostMapping("/login")
    @Operation(summary = "Connexion d'un utilisateur")
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "200", description = "Connexion réussie"),
        @SwaggerApiResponse(responseCode = "401", description = "Identifiants invalides"),
        @SwaggerApiResponse(responseCode = "403", description = "Compte désactivé")
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
        @SwaggerApiResponse(responseCode = "200", description = "Token rafraîchi avec succès"),
        @SwaggerApiResponse(responseCode = "401", description = "Token de rafraîchissement invalide")
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
     * Déconnexion de l'utilisateur
     */
    @PostMapping("/logout")
    @Operation(summary = "Déconnexion de l'utilisateur")
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "200", description = "Déconnexion réussie")
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
     * Validation du token d'accès
     */
    @GetMapping("/validate")
    @Operation(summary = "Validation du token d'accès")
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "200", description = "Token valide"),
        @SwaggerApiResponse(responseCode = "401", description = "Token invalide")
    })
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            String token = authHeader.startsWith("Bearer ") ? 
                authHeader.substring(7) : authHeader;
            
            boolean isValid = authService.validateToken(token);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                isValid ? "Token valide" : "Token invalide",
                isValid
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                    false,
                    "Token invalide",
                    false
                ));
        }
    }

    /**
     * Initiation de l'authentification OAuth2 Google
     */
    @GetMapping("/oauth2/google")
    @Operation(summary = "Redirection vers l'authentification Google")
    public void googleOAuth2(HttpServletResponse response) throws IOException {
        String googleAuthUrl = authService.getGoogleAuthUrl();
        response.sendRedirect(googleAuthUrl);
    }

    /**
     * Initiation de l'authentification OAuth2 Facebook
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
        @SwaggerApiResponse(responseCode = "200", description = "Authentification OAuth2 réussie"),
        @SwaggerApiResponse(responseCode = "400", description = "Erreur d'authentification OAuth2")
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
     * Demande de réinitialisation de mot de passe
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Demande de réinitialisation de mot de passe")
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "200", description = "Email de réinitialisation envoyé"),
        @SwaggerApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Parameter(description = "Email de l'utilisateur")
            @RequestParam String email) {
        
        try {
            authService.initiatePasswordReset(email);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Si cet email existe, un lien de réinitialisation a été envoyé",
                null
            ));
        } catch (Exception e) {
            // Ne pas révéler si l'email existe ou non pour des raisons de sécurité
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Si cet email existe, un lien de réinitialisation a été envoyé",
                null
            ));
        }
    }

    /**
     * Réinitialisation du mot de passe
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialisation du mot de passe")
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "200", description = "Mot de passe réinitialisé avec succès"),
        @SwaggerApiResponse(responseCode = "400", description = "Token invalide ou expiré")
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Parameter(description = "Token de réinitialisation")
            @RequestParam String token,
            @Parameter(description = "Nouveau mot de passe")
            @RequestParam String newPassword) {
        
        try {
            authService.resetPassword(token, newPassword);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Mot de passe réinitialisé avec succès",
                null
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
     * Vérification de l'email
     */
    @GetMapping("/verify-email")
    @Operation(summary = "Vérification de l'email")
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "200", description = "Email vérifié avec succès"),
        @SwaggerApiResponse(responseCode = "400", description = "Token de vérification invalide")
    })
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Parameter(description = "Token de vérification")
            @RequestParam String token) {
        
        try {
            authService.verifyEmail(token);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Email vérifié avec succès",
                null
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
     * Renvoyer l'email de vérification
     */
    @PostMapping("/resend-verification")
    @Operation(summary = "Renvoyer l'email de vérification")
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "200", description = "Email de vérification renvoyé")
    })
    public ResponseEntity<ApiResponse<Void>> resendVerificationEmail(
            @Parameter(description = "Email de l'utilisateur")
            @RequestParam String email) {
        
        try {
            authService.resendVerificationEmail(email);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Email de vérification renvoyé",
                null
            ));
        } catch (Exception e) {
            // Ne pas révéler si l'email existe ou non
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Si cet email existe et n'est pas encore vérifié, un email a été renvoyé",
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