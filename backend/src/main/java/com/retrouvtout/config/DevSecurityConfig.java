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
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@Profile("dev")
public class DevSecurityConfig {

    @Autowired(required = false)
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired(required = false)  
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        System.out.println("🔧 DEV Security Configuration - Using Fixed CORS");
        
        HttpSecurity httpSecurity = http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .exceptionHandling(exception -> {
                if (jwtAuthenticationEntryPoint != null) {
                    exception.authenticationEntryPoint(jwtAuthenticationEntryPoint);
                }
            })
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/health", "/actuator/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/test/**", "/api/ping", "/api/cors-test").permitAll()
                .requestMatchers("/api/debug/**", "/api/db-test/**").permitAll()
                .requestMatchers("/api/auth-debug/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                .requestMatchers("/files/**", "/uploads/**", "/static/**").permitAll()
                .requestMatchers("/.well-known/**", "/favicon.ico").permitAll()
                .requestMatchers("GET", "/api/listings").permitAll()
                .requestMatchers("GET", "/api/listings/{id}").permitAll()
                .requestMatchers("/api/users/me").authenticated()
                .requestMatchers("PUT", "/api/users/me").authenticated()
                .requestMatchers("POST", "/api/listings").authenticated()
                .requestMatchers("PUT", "/api/listings/**").authenticated()
                .requestMatchers("DELETE", "/api/listings/**").authenticated()
                .requestMatchers("/api/messages/**").authenticated()
                .requestMatchers("/api/threads/**").authenticated()
                .requestMatchers("POST", "/api/upload/**").authenticated()
                .requestMatchers("/api/notifications/**").authenticated()
                .requestMatchers("/api/**").permitAll()
                .anyRequest().permitAll()
            );

        if (jwtAuthenticationFilter != null) {
            httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            System.out.println("✅ JWT Filter added to security chain");
        }
        
        return httpSecurity.build();
    }
}