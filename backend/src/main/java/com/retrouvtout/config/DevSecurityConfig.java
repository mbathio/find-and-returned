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
 * ✅ CONFIGURATION DE SÉCURITÉ SIMPLIFIÉE POUR LE DÉVELOPPEMENT
 * Configuration ultra-permissive pour éviter les blocages en dev
 */
@Configuration
@EnableWebSecurity
@Profile("dev")
public class DevSecurityConfig {

    /**
     * ✅ Bean PasswordEncoder OBLIGATOIRE
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * ✅ Configuration de sécurité ULTRA-PERMISSIVE pour le développement
     */
    @Bean
    @Order(1)
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        System.out.println("🔧 Configuration de sécurité DEV - Mode permissif");
        
        return http
            // ✅ Désactiver CSRF complètement
            .csrf(csrf -> csrf.disable())
            
            // ✅ Configuration CORS ultra-permissive
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // ✅ Autoriser ABSOLUMENT TOUT en développement
            .authorizeHttpRequests(authz -> authz
                // Health checks
                .requestMatchers("/", "/health", "/actuator/**").permitAll()
                
                // API d'authentification - TOUT AUTORISER
                .requestMatchers("/api/auth/**").permitAll()
                
                // Tests de DB
                .requestMatchers("/api/db-test/**").permitAll()
                
                // API de test
                .requestMatchers("/api/test/**").permitAll()
                .requestMatchers("/api/ping").permitAll()
                .requestMatchers("/api/cors-test").permitAll()
                
                // Tous les endpoints API (permissif en dev)
                .requestMatchers("/api/**").permitAll()
                
                // Swagger/OpenAPI
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                
                // Fichiers statiques
                .requestMatchers("/files/**", "/uploads/**", "/static/**").permitAll()
                
                // Chrome DevTools et autres
                .requestMatchers("/.well-known/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                
                // AUTORISER TOUT LE RESTE
                .anyRequest().permitAll()
            )
            
            // ✅ Session stateless pour les API REST
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // ✅ Désactiver toutes les protections en dev
            .headers(headers -> headers
                .frameOptions().disable()
                .contentTypeOptions().disable()
                .httpStrictTransportSecurity().disable())
            
            // ✅ Pas d'authentification de base
            .httpBasic().disable()
            .formLogin().disable()
            .logout().disable()
            
            .build();
    }

    /**
     * ✅ Configuration CORS ULTRA-PERMISSIVE pour le développement
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("🔧 Configuration CORS - Mode ultra-permissif");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // ✅ Autoriser TOUTES les origines
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedOrigins(List.of(
            "http://localhost:8080",
            "http://localhost:3000", 
            "http://localhost:5173",
            "http://127.0.0.1:8080",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5173"
        ));
        
        // ✅ Autoriser TOUTES les méthodes HTTP
        configuration.setAllowedMethods(List.of("*"));
        
        // ✅ Autoriser TOUS les headers
        configuration.setAllowedHeaders(List.of("*"));
        
        // ✅ Headers exposés pour les API
        configuration.setExposedHeaders(List.of(
            "Authorization", 
            "Cache-Control", 
            "Content-Type",
            "X-Total-Count", 
            "X-Page-Number", 
            "X-Page-Size",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Methods",
            "Access-Control-Allow-Headers"
        ));
        
        // ✅ Autoriser les credentials
        configuration.setAllowCredentials(true);
        
        // ✅ Cache CORS pendant 1 heure
        configuration.setMaxAge(3600L);

        // ✅ Appliquer à TOUTES les routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}