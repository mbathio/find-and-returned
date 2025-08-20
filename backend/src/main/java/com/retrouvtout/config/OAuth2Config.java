package com.retrouvtout.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configuration de sécurité pour l'environnement de développement
     * Désactive OAuth2 pour simplifier le développement
     */
    @Configuration
    @Profile({"dev", "test"})
    public static class DevSecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/api/public/**", "/actuator/**", "/swagger-ui/**", "/api-docs/**").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/**").permitAll() // Permissif pour le dev
                    .anyRequest().permitAll()
                )
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

            return http.build();
        }
    }

    /**
     * Configuration de sécurité pour la production avec OAuth2
     */
    @Configuration
    @Profile("prod")
    @ConditionalOnProperty(
        name = "spring.security.oauth2.client.registration.google.client-id",
        matchIfMissing = false
    )
    public static class ProdSecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/api/public/**", "/actuator/health").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                    .defaultSuccessUrl("/api/auth/oauth2/success")
                    .failureUrl("/api/auth/oauth2/failure")
                );

            return http.build();
        }
    }
}