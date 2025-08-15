// SchedulingService.java - Gestion des tâches planifiées
package com.retrouvtout.service;

import com.retrouvtout.repository.AlertRepository;
import com.retrouvtout.repository.ConfirmationRepository;
import com.retrouvtout.repository.ListingRepository;
import com.retrouvtout.repository.PushSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service pour les tâches planifiées
 */
@Service
@Transactional
public class SchedulingService {

    private final ConfirmationRepository confirmationRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final FileUploadService fileUploadService;

    @Autowired
    public SchedulingService(ConfirmationRepository confirmationRepository,
                           PushSubscriptionRepository pushSubscriptionRepository,
                           FileUploadService fileUploadService) {
        this.confirmationRepository = confirmationRepository;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.fileUploadService = fileUploadService;
    }

    /**
     * Nettoyer les confirmations expirées (toutes les heures)
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredConfirmations() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
            confirmationRepository.deleteByExpiresAtBeforeAndUsedAtIsNull(cutoff);
            System.out.println("Nettoyage des confirmations expirées terminé");
        } catch (Exception e) {
            System.err.println("Erreur lors du nettoyage des confirmations: " + e.getMessage());
        }
    }

    /**
     * Nettoyer les abonnements push inactifs (quotidien)
     */
    @Scheduled(cron = "0 0 2 * * *") // 2h du matin
    public void cleanupInactivePushSubscriptions() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
            pushSubscriptionRepository.deleteByLastUsedAtBefore(cutoff);
            System.out.println("Nettoyage des abonnements push inactifs terminé");
        } catch (Exception e) {
            System.err.println("Erreur lors du nettoyage des abonnements push: " + e.getMessage());
        }
    }

    /**
     * Nettoyer les fichiers temporaires (quotidien)
     */
    @Scheduled(cron = "0 0 3 * * *") // 3h du matin
    public void cleanupTempFiles() {
        try {
            fileUploadService.cleanupTempFiles();
            System.out.println("Nettoyage des fichiers temporaires terminé");
        } catch (Exception e) {
            System.err.println("Erreur lors du nettoyage des fichiers: " + e.getMessage());
        }
    }
}
