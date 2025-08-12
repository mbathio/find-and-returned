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
            );

        // Ajouter le filtre JWT avant le filtre d'authentification par nom d'utilisateur/mot de passe
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuration des en-têtes de sécurité
     */
    @Bean
    public SecurityFilterChain securityHeaders(HttpSecurity http) throws Exception {
        http.headers(headers -> headers
            .frameOptions().deny()
            .contentTypeOptions().and()
            .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                .maxAgeInSeconds(31536000)
                .includeSubdomains(true))
            .referrerPolicy().and()
            .permissionsPolicy(permissions -> permissions
                .policy("camera=(), microphone=(), geolocation=(self)"))
        );
        
        return http.build();
    }
}

// Configuration JWT Token Provider
package com.retrouvtout.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Fournisseur de tokens JWT
 */
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshTokenExpirationInMs;

    private static final String ISSUER = "retrouvtout-api";
    private static final String AUDIENCE = "retrouvtout-app";

    /**
     * Générer un token d'accès
     */
    public String generateToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

        return JWT.create()
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withSubject(userId)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim("type", "access")
                .sign(algorithm);
    }

    /**
     * Générer un token de rafraîchissement
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationInMs);

        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

        return JWT.create()
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withSubject(userId)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim("type", "refresh")
                .sign(algorithm);
    }

    /**
     * Générer un token de réinitialisation de mot de passe
     */
    public String generatePasswordResetToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (60 * 60 * 1000)); // 1 heure

        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

        return JWT.create()
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withSubject(userId)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim("type", "password_reset")
                .sign(algorithm);
    }

    /**
     * Générer un token de vérification d'email
     */
    public String generateEmailVerificationToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (24 * 60 * 60 * 1000)); // 24 heures

        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

        return JWT.create()
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withSubject(userId)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim("type", "email_verification")
                .sign(algorithm);
    }

    /**
     * Extraire l'ID utilisateur du token
     */
    public String getUserIdFromToken(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Token invalide", e);
        }
    }

    /**
     * Obtenir la date d'expiration du token
     */
    public Date getExpirationFromToken(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            return decodedJWT.getExpiresAt();
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Token invalide", e);
        }
    }

    /**
     * Valider un token
     */
    public boolean validateToken(String token) {
        try {
            verifyToken(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * Vérifier et décoder un token
     */
    private DecodedJWT verifyToken(String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .build();

        return verifier.verify(token);
    }

    /**
     * Obtenir le type de token
     */
    public String getTokenType(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            return decodedJWT.getClaim("type").asString();
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Token invalide", e);
        }
    }

    /**
     * Vérifier si le token est du type spécifié
     */
    public boolean isTokenOfType(String token, String type) {
        try {
            String tokenType = getTokenType(token);
            return type.equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }
}