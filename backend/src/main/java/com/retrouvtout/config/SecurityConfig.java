package com.retrouvtout.config;

import com.retrouvtout.security.JwtAuthenticationEntryPoint;
import com.retrouvtout.security.JwtAuthenticationFilter;
import com.retrouvtout.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration de sécurité Spring Security
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age}")
    private long maxAge;

    @Autowired
    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                         JwtAuthenticationEntryPoint unauthorizedHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    /**
     * Configuration du filtre JWT
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * Configuration de l'encodeur de mots de passe
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configuration du gestionnaire d'authentification
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configuration de CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origines autorisées
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOriginPatterns(origins);
        
        // Méthodes autorisées
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);
        
        // Headers autorisés
        if ("*".equals(allowedHeaders)) {
            configuration.addAllowedHeader("*");
        } else {
            List<String> headers = Arrays.asList(allowedHeaders.split(","));
            configuration.setAllowedHeaders(headers);
        }
        
        // Headers exposés
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Cache-Control", "Content-Type", 
            "X-Total-Count", "X-Page-Number", "X-Page-Size"
        ));
        
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Configuration de la chaîne de filtres de sécurité
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configuration CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Désactiver CSRF pour les API REST
            .csrf(AbstractHttpConfigurer::disable)
            
            // Point d'entrée pour les erreurs d'authentification
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedHandler))
            
            // Gestion de session stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configuration des autorisations
            .authorizeHttpRequests(authz -> authz
                // Endpoints publics
                .requestMatchers("/").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // Documentation API
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                
                // Authentification
                .requestMatchers("/auth/**").permitAll()
                
                // Endpoints publics pour les annonces (lecture seule)
                .requestMatchers(HttpMethod.GET, "/listings").permitAll()
                .requestMatchers(HttpMethod.GET, "/listings/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/listings/user/{userId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/listings/{id}/similar").permitAll()
                .requestMatchers(HttpMethod.POST, "/listings/{id}/view").permitAll()
                
                // Upload public (pour la prévisualisation)
                .requestMatchers(HttpMethod.POST, "/upload/temp").permitAll()
                
                // Profils publics
                .requestMatchers(HttpMethod.GET, "/users/{id}/public").permitAll()
                
                // Websocket (sera géré séparément)
                .requestMatchers("/ws/**").permitAll()
                
                // Endpoints protégés - utilisateurs authentifiés
                .requestMatchers(HttpMethod.POST, "/listings").hasRole("USER")
                .requestMatchers(HttpMethod.PUT, "/listings/**").hasRole("USER")
                .requestMatchers(HttpMethod.DELETE, "/listings/**").hasRole("USER")
                .requestMatchers(HttpMethod.PATCH, "/listings/**").hasRole("USER")
                
                .requestMatchers("/users/me").hasRole("USER")
                .requestMatchers(HttpMethod.PUT, "/users/me").hasRole("USER")
                .requestMatchers("/users/me/stats").hasRole("USER")
                .requestMatchers("/users/me/password").hasRole("USER")
                
                .requestMatchers("/threads/**").hasRole("USER")
                .requestMatchers("/messages/**").hasRole("USER")
                .requestMatchers("/alerts/**").hasRole("USER")
                .requestMatchers("/confirmations/**").hasRole("USER")
                
                .requestMatchers(HttpMethod.POST, "/upload/**").hasRole("USER")
                
                // Endpoints de modération - modérateurs
                .requestMatchers("/moderation/**").hasRole("MODERATOR")
                
                // Endpoints d'administration - administrateurs
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/users/search").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
                
                // Tout le reste nécessite une authentification
                .anyRequest().authenticated()
            )
            
            // Configuration des en-têtes de sécurité
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .contentTypeOptions(Customizer.withDefaults())
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true))
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            );

        // Ajouter le filtre JWT avant le filtre d'authentification par nom d'utilisateur/mot de passe
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}