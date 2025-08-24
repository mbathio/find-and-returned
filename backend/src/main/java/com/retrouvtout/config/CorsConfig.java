package com.retrouvtout.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * ✅ CONFIGURATION CORS GLOBALE AMÉLIORÉE
 * Résout les problèmes de CORS avec les requêtes authentifiées
 */
@Configuration
@Profile("dev")
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:8080}")
    private String allowedOrigins;

    /**
     * ✅ Configuration CORS globale pour tous les contrôleurs
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        System.out.println("🔧 Configuration CORS globale activée");
        
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // ✅ ULTRA-PERMISSIF pour dev
                .allowedOrigins(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders(
                    "Authorization", 
                    "Cache-Control", 
                    "Content-Type",
                    "Access-Control-Allow-Origin",
                    "Access-Control-Allow-Methods",
                    "Access-Control-Allow-Headers",
                    "Access-Control-Allow-Credentials"
                )
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * ✅ Bean CORS pour Spring Security
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("🔧 Configuration CORS pour Spring Security");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // ✅ ULTRA-PERMISSIF pour le développement
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // ✅ Origines spécifiques aussi
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        
        // ✅ TOUS les headers
        configuration.setAllowedHeaders(List.of("*"));
        
        // ✅ TOUTES les méthodes HTTP
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        
        // ✅ Headers exposés pour l'authentification
        configuration.setExposedHeaders(List.of(
            "Authorization", 
            "Cache-Control", 
            "Content-Type",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Methods", 
            "Access-Control-Allow-Headers",
            "Access-Control-Allow-Credentials"
        ));
        
        // ✅ CRITIQUE: Permettre les credentials pour JWT
        configuration.setAllowCredentials(true);
        
        // ✅ Cache de 1 heure
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}