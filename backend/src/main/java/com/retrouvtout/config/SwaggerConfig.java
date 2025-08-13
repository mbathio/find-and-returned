package com.retrouvtout.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration pour la documentation API avec Swagger/OpenAPI
 */
@Configuration
public class SwaggerConfig {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                    new Server().url("http://localhost:" + serverPort + "/api").description("Serveur de développement"),
                    new Server().url("https://api.retrouvtout.com").description("Serveur de production")
                ))
                .components(new Components()
                    .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Token JWT pour l'authentification")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    private Info apiInfo() {
        return new Info()
                .title("Retrouv'Tout API")
                .description("API REST pour la plateforme Retrouv'Tout - Objets perdus et retrouvés")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Équipe Retrouv'Tout")
                    .email("contact@retrouvtout.com")
                    .url("https://retrouvtout.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT"));
    }
}