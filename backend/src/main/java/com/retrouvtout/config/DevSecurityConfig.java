// backend/src/main/java/com/retrouvtout/config/DevSecurityConfig.java - CORRIGÉ
package com.retrouvtout.config;

import com.retrouvtout.security.JwtAuthenticationFilter;
import com.retrouvtout.security.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * ✅ CONFIGURATION DE SÉCURITÉ DEV CORRIGÉE
 * Utilise JWT pour les endpoints protégés mais plus permissive sur les erreurs
 */
@Configuration
@EnableWebSecurity
@Profile("dev")
public class DevSecurityConfig {

    @Autowired(required = false)
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired(required = false)  
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * ✅ Bean PasswordEncoder OBLIGATOIRE
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * ✅ Configuration de sécurité DEV avec JWT mais permissive
     */
    @Bean
    @Order(1)
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        System.out.println("🔧 Configuration de sécurité DEV - Mode permissif AVEC JWT");
        
        HttpSecurity httpSecurity = http
            // ✅ Désactiver CSRF complètement
            .csrf(csrf -> csrf.disable())
            
            // ✅ Configuration CORS ultra-permissive
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // ✅ Point d'entrée pour l'authentification (plus permissif en dev)
            .exceptionHandling(exception -> {
                if (jwtAuthenticationEntryPoint != null) {
                    exception.authenticationEntryPoint(jwtAuthenticationEntryPoint);
                }
            })
            
            // ✅ Session stateless pour JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // ✅ Configuration des autorisations - AVEC authentification pour certains endpoints
            .authorizeHttpRequests(authz -> authz
                // Endpoints complètement publics
                .requestMatchers("/", "/health", "/actuator/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll() // Login/register
                .requestMatchers("/api/test/**", "/api/ping", "/api/cors-test").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                .requestMatchers("/files/**", "/uploads/**", "/static/**").permitAll()
                .requestMatchers("/.well-known/**", "/favicon.ico").permitAll()
                
                // ✅ Endpoints qui NÉCESSITENT l'authentification JWT
                .requestMatchers("/api/users/me").authenticated() // ✅ IMPORTANT!
                .requestMatchers("/api/messages/**").authenticated()
                .requestMatchers("/api/threads/**").authenticated()
                
                // ✅ Tous les autres endpoints API permis en dev (pour éviter les blocages)
                .requestMatchers("/api/**").permitAll()
                
                // Tout le reste permis
                .anyRequest().permitAll()
            );

        // ✅ CRUCIAL: Ajouter le filtre JWT SEULEMENT s'il existe
        if (jwtAuthenticationFilter != null) {
            httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            System.out.println("✅ Filtre JWT ajouté à la chaîne de sécurité");
        } else {
            System.out.println("⚠️ JwtAuthenticationFilter non trouvé - JWT désactivé");
        }
        
        return httpSecurity.build();
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