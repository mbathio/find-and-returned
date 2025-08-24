package com.retrouvtout.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * ✅ CONFIGURATION CORS UNIFIÉE - Une seule source de vérité
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:8080,http://localhost:3000,http://localhost:5173,http://127.0.0.1:8080,http://127.0.0.1:3000,http://127.0.0.1:5173}")
    private String allowedOrigins;

    @Value("${app.cors.dev-mode:false}")
    private boolean devMode;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("🔧 CORS Unified Configuration");
        
        CorsConfiguration config = new CorsConfiguration();
        
        // ✅ SOLUTION : setAllowedOriginPatterns pour plus de flexibilité
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOriginPatterns(origins);
        
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        config.setExposedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        // En mode dev, on peut être plus permissif
        if (devMode) {
            config.addAllowedOriginPattern("http://localhost:*");
            config.addAllowedOriginPattern("http://127.0.0.1:*");
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
