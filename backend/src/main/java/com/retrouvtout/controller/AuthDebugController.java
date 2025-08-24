package com.retrouvtout.controller;

import com.retrouvtout.security.UserPrincipal;
import com.retrouvtout.security.JwtTokenProvider;
import com.retrouvtout.dto.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ CONTRÔLEUR DE DEBUG POUR L'AUTHENTIFICATION JWT
 * Aide à diagnostiquer les problèmes d'authentification
 */
@RestController
@RequestMapping("/api/auth-debug")
@CrossOrigin(origins = {"*"})
public class AuthDebugController {

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * ✅ Tester l'authentification actuelle
     */
    @GetMapping("/current-user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentAuthUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        System.out.println("🔧 AUTH DEBUG - getCurrentAuthUser");
        
        Map<String, Object> debugInfo = new HashMap<>();
        
        try {
            // ✅ Informations de la requête
            debugInfo.put("requestUri", request.getRequestURI());
            debugInfo.put("method", request.getMethod());
            debugInfo.put("authorizationHeader", request.getHeader("Authorization"));
            
            // ✅ Informations de SecurityContext
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            debugInfo.put("hasAuthentication", auth != null);
            debugInfo.put("isAuthenticated", auth != null ? auth.isAuthenticated() : false);
            debugInfo.put("authType", auth != null ? auth.getClass().getSimpleName() : "null");
            debugInfo.put("authorities", auth != null ? auth.getAuthorities().toString() : "null");
            
            // ✅ Informations UserPrincipal
            debugInfo.put("hasUserPrincipal", userPrincipal != null);
            if (userPrincipal != null) {
                debugInfo.put("principalId", userPrincipal.getId());
                debugInfo.put("principalName", userPrincipal.getName());
                debugInfo.put("principalEmail", userPrincipal.getEmail());
                debugInfo.put("principalAuthorities", userPrincipal.getAuthorities().toString());
            }
            
            // ✅ Test du token JWT
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    boolean isValid = tokenProvider.validateToken(token);
                    debugInfo.put("tokenValid", isValid);
                    
                    if (isValid) {
                        String userIdFromToken = tokenProvider.getUserIdFromToken(token);
                        debugInfo.put("tokenUserId", userIdFromToken);
                        debugInfo.put("tokenExpiration", tokenProvider.getExpirationDateFromToken(token));
                    }
                } catch (Exception e) {
                    debugInfo.put("tokenError", e.getMessage());
                }
            } else {
                debugInfo.put("tokenStatus", "Aucun token Bearer trouvé");
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Debug authentification", debugInfo));
            
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
            return ResponseEntity.ok(new ApiResponse<>(false, "Erreur debug", debugInfo));
        }
    }

    /**
     * ✅ Endpoint protégé pour tester l'authentification
     */
    @GetMapping("/protected-test")
    public ResponseEntity<ApiResponse<String>> protectedTest(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        System.out.println("🔧 AUTH DEBUG - protectedTest");
        
        if (userPrincipal == null) {
            return ResponseEntity.status(401)
                .body(new ApiResponse<>(false, "Utilisateur non authentifié", null));
        }
        
        String message = String.format("Utilisateur authentifié: %s (%s)", 
            userPrincipal.getName(), userPrincipal.getId());
        
        return ResponseEntity.ok(new ApiResponse<>(true, message, "Test réussi"));
    }

    /**
     * ✅ Valider un token JWT spécifique
     */
    @PostMapping("/validate-token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            String token = request.get("token");
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Token manquant", null));
            }
            
            System.out.println("🔧 Validation token: " + token.substring(0, Math.min(token.length(), 20)) + "...");
            
            boolean isValid = tokenProvider.validateToken(token);
            result.put("valid", isValid);
            
            if (isValid) {
                try {
                    String userId = tokenProvider.getUserIdFromToken(token);
                    result.put("userId", userId);
                    result.put("expiration", tokenProvider.getExpirationDateFromToken(token));
                    result.put("isExpired", tokenProvider.isTokenExpired(token));
                } catch (Exception e) {
                    result.put("extractionError", e.getMessage());
                }
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Token validé", result));
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.ok(new ApiResponse<>(false, "Erreur validation", result));
        }
    }

    /**
     * ✅ Informations sur la configuration JWT
     */
    @GetMapping("/jwt-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getJwtInfo() {
        
        Map<String, Object> info = new HashMap<>();
        
        try {
            info.put("jwtProviderClass", tokenProvider.getClass().getSimpleName());
            info.put("timestamp", System.currentTimeMillis());
            info.put("note", "Configuration JWT active");
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Informations JWT", info));
            
        } catch (Exception e) {
            info.put("error", e.getMessage());
            return ResponseEntity.ok(new ApiResponse<>(false, "Erreur JWT info", info));
        }
    }
}