package com.retrouvtout.service;

import com.retrouvtout.entity.User;
import com.retrouvtout.entity.Listing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service de notifications conforme au cahier des charges - Section 3.3
 * Alertes email/SMS et notifications push
 */
@Service
public class NotificationService {

    private final EmailService emailService;
    private final SmsService smsService;

    @Value("${app.notifications.email.enabled:true}")
    private boolean emailNotificationsEnabled;

    @Value("${app.notifications.sms.enabled:false}")
    private boolean smsNotificationsEnabled;

    @Value("${app.notifications.push.enabled:false}")
    private boolean pushNotificationsEnabled;

    @Autowired
    public NotificationService(EmailService emailService, SmsService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }

    /**
     * Envoyer notification email - Section 3.3
     * Alertes email aux propriétaires lorsqu'un objet correspondant est retrouvé
     */
    @Async
    public void sendEmailAlert(User user, Listing listing) {
        if (!emailNotificationsEnabled || !user.getEmailVerified()) {
            return;
        }

        try {
            String subject = "Objet retrouvé correspondant à votre recherche - Retrouv'Tout";
            String message = String.format(
                "Bonjour %s,\n\nUn objet correspondant à votre recherche a été trouvé :\n\n" +
                "Titre : %s\nLieu : %s\nCatégorie : %s\n\n" +
                "Connectez-vous à votre compte pour contacter la personne qui l'a trouvé.\n\n" +
                "Cordialement,\nL'équipe Retrouv'Tout",
                user.getName(), listing.getTitle(), listing.getLocationText(), 
                listing.getCategory().getValue()
            );

            emailService.sendNotificationEmail(user, subject, message);
        } catch (Exception e) {
            System.err.println("Erreur envoi notification email: " + e.getMessage());
        }
    }

    /**
     * Envoyer notification SMS - Section 3.3
     * Alertes SMS aux propriétaires
     */
    @Async
    public void sendSmsAlert(User user, Listing listing) {
        if (!smsNotificationsEnabled || user.getPhone() == null || user.getPhone().isEmpty()) {
            return;
        }

        try {
            String message = String.format(
                "Retrouv'Tout: Objet trouvé correspondant à votre recherche: %s à %s. Connectez-vous pour plus d'infos.",
                listing.getTitle(), listing.getLocationText()
            );

            smsService.sendSms(user.getPhone(), message);
        } catch (Exception e) {
            System.err.println("Erreur envoi notification SMS: " + e.getMessage());
        }
    }

    /**
     * Envoyer notification push - Section 3.3
     * Notifications push sur l'application
     */
    @Async
    public void sendPushNotification(String userId, String title, String body, String url) {
        if (!pushNotificationsEnabled) {
            return;
        }

        try {
            // Implémentation basique pour les notifications push
            // En production, utiliser Firebase Cloud Messaging ou service similaire
            System.out.println(String.format(
                "Notification Push pour %s: %s - %s (URL: %s)",
                userId, title, body, url
            ));
        } catch (Exception e) {
            System.err.println("Erreur envoi notification push: " + e.getMessage());
        }
    }

    /**
     * Notifier quand un objet correspondant est trouvé - Section 3.3
     * Envoi automatique de notifications aux propriétaires concernés
     */
    @Async
    public void notifyObjectFound(User user, Listing listing) {
        // Email
        sendEmailAlert(user, listing);
        
        // SMS
        sendSmsAlert(user, listing);
        
        // Push
        sendPushNotification(
            user.getId(), 
            "Objet trouvé !", 
            String.format("Objet correspondant trouvé : %s", listing.getTitle()),
            "/annonces/" + listing.getId()
        );
    }

    /**
     * Notifier d'un nouveau message - Section 3.5 (messagerie intégrée)
     */
    @Async
    public void notifyNewMessage(User recipient, User sender, String listingTitle) {
        if (emailNotificationsEnabled && recipient.getEmailVerified()) {
            try {
                String subject = "Nouveau message - Retrouv'Tout";
                String message = String.format(
                    "Bonjour %s,\n\nVous avez reçu un nouveau message de %s concernant: %s\n\n" +
                    "Connectez-vous pour répondre.\n\nCordialement,\nL'équipe Retrouv'Tout",
                    recipient.getName(), sender.getName(), listingTitle
                );

                emailService.sendNotificationEmail(recipient, subject, message);
            } catch (Exception e) {
                System.err.println("Erreur envoi notification message: " + e.getMessage());
            }
        }

        // Notification push pour nouveau message
        sendPushNotification(
            recipient.getId(), 
            "Nouveau message", 
            String.format("Message de %s", sender.getName()),
            "/messages"
        );
    }
}