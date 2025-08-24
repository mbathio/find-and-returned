
package com.retrouvtout.config;

import com.retrouvtout.security.JwtAuthenticationEntryPoint;
import com.retrouvtout.security.JwtAuthenticationFilter;
import com.retrouvtout.security.CustomUserDetailsService;
import com.retrouvtout.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * ✅ CONFIGURATION SÉCURITÉ UNIFIÉE - Dev et Prod
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtTokenProvider tokenProvider;
    private final CorsConfigurationSource corsConfigurationSource;

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @Autowired
    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                         JwtAuthenticationEntryPoint unauthorizedHandler,
                         JwtTokenProvider tokenProvider,
                         CorsConfigurationSource corsConfigurationSource) {
        this.customUserDetailsService = customUserDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.tokenProvider = tokenProvider;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, customUserDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("🔧 Security Configuration for profile: " + activeProfile);
        
        HttpSecurity httpSecurity = http
            // ✅ CORS : Une seule source de configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> {
                // Endpoints publics communs
                authz.requestMatchers("/", "/health", "/actuator/**").permitAll()
                     .requestMatchers("/api/auth/**").permitAll()
                     .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                     .requestMatchers("/files/**", "/uploads/**", "/static/**").permitAll()
                     .requestMatchers("/.well-known/**", "/favicon.ico").permitAll();

                // Mode développement : plus permissif
                if ("dev".equals(activeProfile)) {
                    authz.requestMatchers("/api/test/**", "/api/ping", "/api/cors-test").permitAll()
                         .requestMatchers("/api/debug/**", "/api/db-test/**").permitAll()
                         .requestMatchers("/api/auth-debug/**").permitAll()
                         .requestMatchers("/api/**").permitAll()
                         .anyRequest().permitAll();
                } else {
                    // Mode production : sécurisé
                    authz.requestMatchers(HttpMethod.GET, "/api/listings").permitAll()
                         .requestMatchers(HttpMethod.GET, "/api/listings/{id}").permitAll()
                         .requestMatchers(HttpMethod.POST, "/api/listings").authenticated()
                         .requestMatchers(HttpMethod.PUT, "/api/listings/**").authenticated()
                         .requestMatchers(HttpMethod.DELETE, "/api/listings/**").authenticated()
                         .requestMatchers("/api/users/me").authenticated()
                         .requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated()
                         .requestMatchers("/api/threads/**").authenticated()
                         .requestMatchers("/api/messages/**").authenticated()
                         .requestMatchers(HttpMethod.POST, "/api/upload/**").authenticated()
                         .requestMatchers("/api/files/**").permitAll()
                         .requestMatchers("/api/notifications/**").authenticated()
                         .requestMatchers("/ws/**").permitAll()
                         .anyRequest().authenticated();
                }
            });

        // Headers de sécurité (surtout en production)
        if (!"dev".equals(activeProfile)) {
            httpSecurity.headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .contentTypeOptions(Customizer.withDefaults())
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)));
        }

        httpSecurity.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
