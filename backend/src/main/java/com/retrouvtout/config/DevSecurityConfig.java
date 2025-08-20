package com.retrouvtout.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration de sécurité pour l'environnement de développement
 * Cette configuration est permissive pour faciliter le développement
 */
@Configuration
@EnableWebSecurity
@Profile("dev")
public class DevSecurityConfig {

    /**
     * Bean PasswordEncoder requis par UserService
     * IMPORTANT: Ce bean est nécessaire même en mode dev
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configuration de sécurité pour le développement
     * Ordre 1 pour avoir priorité sur les autres configurations
     */
    @Bean
    @Order(1)
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        return http
            // Désactiver CSRF pour faciliter les tests API
            .csrf(csrf -> csrf.disable())
            
            // Configuration CORS permissive pour le développement
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Autoriser tous les endpoints en développement
            .authorizeHttpRequests(authz -> authz
                // Endpoints publics de base
                .requestMatchers("/", "/health", "/actuator/**").permitAll()
                
                // API d'authentification
                .requestMatchers("/api/auth/**").permitAll()
                
                // Tous les endpoints API (permissif en dev)
                .requestMatchers("/api/**").permitAll()
                
                // Chrome DevTools (éviter les erreurs 404)
                .requestMatchers("/.well-known/**").permitAll()
                
                // Swagger/OpenAPI
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                
                // Fichiers statiques
                .requestMatchers("/files/**", "/uploads/**").permitAll()
                
                // Autoriser tout le reste
                .anyRequest().permitAll()
            )
            
            // Session stateless pour les API REST
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Désactiver les en-têtes de sécurité stricts en dev
            .headers(headers -> headers
                .frameOptions().disable()
                .contentTypeOptions().disable())
            
            .build();
    }

    /**
     * Configuration CORS permissive pour le développement
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Autoriser toutes les origines localhost et 127.0.0.1
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:*", 
            "http://127.0.0.1:*",
            "https://localhost:*",
            "https://127.0.0.1:*"
        ));
        
        // Autoriser toutes les méthodes HTTP
        configuration.setAllowedMethods(List.of("*"));
        
        // Autoriser tous les headers
        configuration.setAllowedHeaders(List.of("*"));
        
        // Headers exposés (utiles pour les API)
        configuration.setExposedHeaders(List.of(
            "Authorization", 
            "Cache-Control", 
            "Content-Type",
            "X-Total-Count", 
            "X-Page-Number", 
            "X-Page-Size"
        ));
        
        // Autoriser les credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache CORS pendant 1 heure
        configuration.setMaxAge(3600L);

        // Appliquer à toutes les routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}