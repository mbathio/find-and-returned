// backend/src/main/java/com/retrouvtout/security/JwtAuthenticationEntryPoint.java
package com.retrouvtout.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ Point d'entrée pour les erreurs d'authentification JWT
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        System.err.println("❌ Erreur d'authentification JWT: " + authException.getMessage());
        System.err.println("📍 URI: " + request.getRequestURI());
        System.err.println("📍 Method: " + request.getMethod());
        
        // Préparer la réponse d'erreur
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Token d'authentification manquant ou invalide");
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("status", 401);
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", request.getRequestURI());

        // Ajouter des détails pour le débogage en dev
        if (isDevEnvironment()) {
            errorResponse.put("details", authException.getMessage());
            errorResponse.put("authorizationHeader", request.getHeader("Authorization"));
        }

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }

    private boolean isDevEnvironment() {
        String profiles = System.getProperty("spring.profiles.active");
        return profiles != null && profiles.contains("dev");
    }
}