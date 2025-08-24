// backend/src/main/java/com/retrouvtout/config/DevSecurityConfig.java - CORRECTION FINALE CORS

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

import java.util.Arrays;
import java.util.List;

/**
 * ✅ CONFIGURATION DE SÉCURITÉ DEV CORRIGÉE - CORS FIXED
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
     * ✅ CONFIGURATION CORS CORRIGÉE - Plus de "*" avec allowCredentials
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("🔧 Configuration CORS DEV - CORRIGÉE pour allowCredentials");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // ✅ CRITIQUE: Utiliser allowedOriginPatterns au lieu de allowedOrigins avec "*"
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // ✅ OU utiliser des origines spécifiques (recommandé pour production)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:8080",
            "http://localhost:3000", 
            "http://localhost:5173",
            "http://127.0.0.1:8080",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5173"
        ));
        
        // ✅ TOUTES les méthodes HTTP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        
        // ✅ TOUS les headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // ✅ Headers exposés pour JWT
        configuration.setExposedHeaders(Arrays.asList(
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
        
        // ✅ CRITIQUE: Permettre les credentials pour JWT
        configuration.setAllowCredentials(true);
        
        // ✅ Cache de 1 heure
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * ✅ Configuration de sécurité DEV avec CORS fixé
     */
    @Bean
    @Order(1)
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        System.out.println("🔧 Configuration de sécurité DEV - JWT avec CORS corrigé");
        
        HttpSecurity httpSecurity = http
            // ✅ Désactiver CSRF complètement
            .csrf(csrf -> csrf.disable())
            
            // ✅ Configuration CORS avec notre bean
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // ✅ Point d'entrée pour l'authentification
            .exceptionHandling(exception -> {
                if (jwtAuthenticationEntryPoint != null) {
                    exception.authenticationEntryPoint(jwtAuthenticationEntryPoint);
                }
            })
            
            // ✅ Session stateless pour JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // ✅ Configuration des autorisations
            .authorizeHttpRequests(authz -> authz
                // Endpoints complètement publics
                .requestMatchers("/", "/health", "/actuator/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/test/**", "/api/ping", "/api/cors-test").permitAll()
                .requestMatchers("/api/debug/**", "/api/db-test/**").permitAll()
                .requestMatchers("/api/auth-debug/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                .requestMatchers("/files/**", "/uploads/**", "/static/**").permitAll()
                .requestMatchers("/.well-known/**", "/favicon.ico").permitAll()
                
                // Endpoints publics en lecture seule
                .requestMatchers("GET", "/api/listings").permitAll()
                .requestMatchers("GET", "/api/listings/{id}").permitAll()
                
                // ✅ CRITIQUE: Endpoints qui NÉCESSITENT l'authentification JWT
                .requestMatchers("/api/users/me").authenticated()
                .requestMatchers("PUT", "/api/users/me").authenticated()
                .requestMatchers("POST", "/api/listings").authenticated() // ✅ Création d'annonce
                .requestMatchers("PUT", "/api/listings/**").authenticated()
                .requestMatchers("DELETE", "/api/listings/**").authenticated()
                .requestMatchers("/api/messages/**").authenticated()
                .requestMatchers("/api/threads/**").authenticated()
                .requestMatchers("POST", "/api/upload/**").authenticated()
                .requestMatchers("/api/notifications/**").authenticated()
                
                // Tous les autres endpoints API permis en dev
                .requestMatchers("/api/**").permitAll()
                
                // Tout le reste permis
                .anyRequest().permitAll()
            );

        // ✅ CRUCIAL: Ajouter le filtre JWT
        if (jwtAuthenticationFilter != null) {
            httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            System.out.println("✅ Filtre JWT ajouté à la chaîne de sécurité");
        } else {
            System.out.println("❌ JwtAuthenticationFilter non trouvé");
        }
        
        return httpSecurity.build();
    }
}