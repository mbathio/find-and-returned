package com.retrouvtout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Application principale Spring Boot pour Retrouv'Tout
 * 
 * @author Équipe Retrouv'Tout
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing // Activation de l'audit automatique des entités
@EnableAsync // Support des méthodes asynchrones
@EnableScheduling // Support des tâches planifiées
@EnableTransactionManagement // Gestion des transactions
@EnableConfigurationProperties // Support des propriétés de configuration
public class RetrouvToutApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetrouvToutApplication.class, args);
    }
}