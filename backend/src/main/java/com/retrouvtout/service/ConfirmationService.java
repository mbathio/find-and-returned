package com.retrouvtout.service;

import com.retrouvtout.dto.response.ConfirmationResponse;
import com.retrouvtout.entity.Confirmation;
import com.retrouvtout.entity.Thread;
import com.retrouvtout.entity.User;
import com.retrouvtout.exception.ResourceNotFoundException;
import com.retrouvtout.repository.ConfirmationRepository;
import com.retrouvtout.repository.ThreadRepository;
import com.retrouvtout.repository.UserRepository;
import com.retrouvtout.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service pour la gestion des confirmations de remise d'objets
 */
@Service
@Transactional
public class ConfirmationService {

    private final ConfirmationRepository confirmationRepository;
    private final ThreadRepository threadRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final SmsService smsService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_HOURS = 24;

    @Autowired
    public ConfirmationService(ConfirmationRepository confirmationRepository,
                              ThreadRepository threadRepository,
                              UserRepository userRepository,
                              ModelMapper modelMapper,
                              SmsService smsService,
                              EmailService emailService,
                              NotificationService notificationService) {
        this.confirmationRepository = confirmationRepository;
        this.threadRepository = threadRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.smsService = smsService;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    /**
     * Générer un code de confirmation pour un thread
     */
    public ConfirmationResponse generateConfirmation(String threadId, String userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        Thread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new ResourceNotFoundException("Thread", "id", threadId));

        // Vérifier que l'utilisateur fait partie du thread
        if (!thread.getOwnerUser().getId().equals(userId) && 
            !thread.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à générer un code pour ce thread");
        }

        // Vérifier que le thread est approuvé
        if (thread.getStatus() != Thread.ThreadStatus.APPROVED) {
            throw new IllegalArgumentException("Le thread doit être approuvé pour générer un code de confirmation");
        }

        // Supprimer l'ancienne confirmation si elle existe
        confirmationRepository.findByThread(thread)
            .ifPresent(confirmationRepository::delete);

        // Générer un nouveau code
        String code = generateRandomCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(EXPIRATION_HOURS);

        Confirmation confirmation = new Confirmation();
        confirmation.setId(java.util.UUID.randomUUID().toString());
        confirmation.setThread(thread);
        confirmation.setCode(code);
        confirmation.setExpiresAt(expiresAt);

        Confirmation savedConfirmation = confirmationRepository.save(confirmation);

        // Envoyer le code aux deux parties
        sendConfirmationCode(thread, code);

        return modelMapper.mapConfirmationToConfirmationResponse(savedConfirmation);
    }

    /**
     * Valider un code de confirmation
     */
    public ConfirmationResponse validateConfirmation(String code, String userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        Confirmation confirmation = confirmationRepository.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Code de confirmation invalide"));

        // Vérifier que le code n'est pas expiré
        if (confirmation.isExpired()) {
            throw new IllegalArgumentException("Code de confirmation expiré");
        }

        // Vérifier que le code n'est pas déjà utilisé
        if (confirmation.isUsed()) {
            throw new IllegalArgumentException("Code de confirmation déjà utilisé");
        }

        Thread thread = confirmation.getThread();

        // Vérifier que l'utilisateur fait partie du thread
        if (!thread.getOwnerUser().getId().equals(userId) && 
            !thread.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à utiliser ce code");
        }

        // Marquer le code comme utilisé
        confirmation.setUsedAt(LocalDateTime.now());
        confirmation.setUsedByUser(user);

        // Marquer le thread comme fermé
        thread.setStatus(Thread.ThreadStatus.CLOSED);

        // Marquer l'annonce comme résolue
        thread.getListing().setStatus(com.retrouvtout.entity.Listing.ListingStatus.RESOLU);

        confirmationRepository.save(confirmation);
        threadRepository.save(thread);

        // Notifier les parties de la remise réussie
        notifySuccessfulHandover(thread, user);

        return modelMapper.mapConfirmationToConfirmationResponse(confirmation);
    }

    /**
     * Obtenir la confirmation d'un thread
     */
    @Transactional(readOnly = true)
    public ConfirmationResponse getThreadConfirmation(String threadId, String userId) {
        Thread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new ResourceNotFoundException("Thread", "id", threadId));

        // Vérifier l'accès
        if (!thread.getOwnerUser().getId().equals(userId) && 
            !thread.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à accéder à cette confirmation");
        }

        Confirmation confirmation = confirmationRepository.findByThread(thread)
            .orElseThrow(() -> new ResourceNotFoundException("Confirmation", "threadId", threadId));

        return modelMapper.mapConfirmationToConfirmationResponse(confirmation);
    }

    /**
     * Générer un code aléatoire
     */
    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        
        return code.toString();
    }

    /**
     * Envoyer le code de confirmation aux parties
     */
    private void sendConfirmationCode(Thread thread, String code) {
        try {
            String listingTitle = thread.getListing().getTitle();
            String message = String.format(
                "Retrouv'Tout: Code de remise pour '%s': %s. Valable 24h.",
                listingTitle, code
            );

            // Envoyer par SMS si les numéros sont disponibles
            if (thread.getOwnerUser().getPhone() != null) {
                smsService.sendSms(thread.getOwnerUser().getPhone(), message);
            }

            if (thread.getFinderUser().getPhone() != null) {
                smsService.sendSms(thread.getFinderUser().getPhone(), message);
            }

            // Envoyer par notification push
            String title = "Code de remise généré";
            String body = String.format("Code: %s (valable 24h)", code);
            String url = "/messages/" + thread.getId();

            notificationService.sendPushNotification(
                thread.getOwnerUser().getId(), title, body, url);
            notificationService.sendPushNotification(
                thread.getFinderUser().getId(), title, body, url);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du code de confirmation: " + e.getMessage());
        }
    }

    /**
     * Notifier les parties de la remise réussie
     */
    private void notifySuccessfulHandover(Thread thread, User validator) {
        try {
            String listingTitle = thread.getListing().getTitle();
            
            // Notifier les deux parties
            String ownerTitle = "Objet récupéré avec succès !";
            String ownerBody = String.format("Vous avez récupéré: %s", listingTitle);
            
            String finderTitle = "Remise confirmée !";
            String finderBody = String.format("Objet remis avec succès: %s", listingTitle);

            notificationService.sendPushNotification(
                thread.getOwnerUser().getId(), ownerTitle, ownerBody, "/profil");
            notificationService.sendPushNotification(
                thread.getFinderUser().getId(), finderTitle, finderBody, "/profil");

            // Envoyer par SMS de confirmation
            String ownerSms = String.format(
                "Retrouv'Tout: Remise confirmée pour '%s'. Merci d'avoir utilisé notre service !",
                listingTitle
            );
            String finderSms = String.format(
                "Retrouv'Tout: Merci d'avoir aidé à retrouver '%s'. Votre geste compte !",
                listingTitle
            );

            if (thread.getOwnerUser().getPhone() != null) {
                smsService.sendSms(thread.getOwnerUser().getPhone(), ownerSms);
            }

            if (thread.getFinderUser().getPhone() != null) {
                smsService.sendSms(thread.getFinderUser().getPhone(), finderSms);
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi des notifications de remise: " + e.getMessage());
        }
    }

    /**
     * Nettoyer les confirmations expirées (tâche planifiée)
     */
    @Scheduled(fixedRate = 3600000) // Toutes les heures
    public void cleanupExpiredConfirmations() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Confirmation> expiredConfirmations = confirmationRepository.findExpiredConfirmations(now);
            
            for (Confirmation confirmation : expiredConfirmations) {
                confirmationRepository.delete(confirmation);
            }
            
            if (!expiredConfirmations.isEmpty()) {
                System.out.println("Nettoyé " + expiredConfirmations.size() + " confirmations expirées");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du nettoyage des confirmations expirées: " + e.getMessage());
        }
    }
}