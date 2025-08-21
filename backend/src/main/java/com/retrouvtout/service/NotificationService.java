package com.retrouvtout.service;

import com.retrouvtout.entity.User;
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
     * Envoyer notification email - Cahier des charges 3.3
     * Alertes email aux propriétaires
     */
    @Async
    public void sendEmailNotification(User user, String subject, String message) {
        if (!emailNotificationsEnabled || !user.getEmailVerified()) {
            return;
        }

        try {
            emailService.sendNotificationEmail(user, subject, message);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification email: " + e.getMessage());
        }
    }

    /**
     * Envoyer notification SMS - Cahier des charges 3.3
     * Alertes SMS aux propriétaires
     */
    @Async
    public void sendSmsNotification(User user, String message) {
        if (!smsNotificationsEnabled || user.getPhone() == null || user.getPhone().isEmpty()) {
            return;
        }

        try {
            smsService.sendSms(user.getPhone(), message);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification SMS: " + e.getMessage());
        }
    }

    /**
     * Envoyer notification push - Cahier des charges 3.3
     * Notifications push sur l'application
     */
    @Async
    public void sendPushNotification(String userId, String title, String body, String url) {
        if (!pushNotificationsEnabled) {
            return;
        }

        try {
            // Implémentation basique pour les notifications push
            System.out.println(String.format(
                "Notification Push pour %s: %s - %s (URL: %s)",
                userId, title, body, url
            ));
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification push: " + e.getMessage());
        }
    }

    /**
     * Notifier quand un objet correspondant est trouvé
     * Conforme au cahier des charges 3.3
     */
    @Async
    public void notifyObjectFound(User user, String objectTitle, String finderName) {
        String emailSubject = "Objet correspondant trouvé - Retrouv'Tout";
        String emailMessage = String.format(
            "Bonjour %s,\n\nUn objet correspondant à votre recherche a été trouvé: %s\n\n" +
            "Connectez-vous à votre compte pour contacter la personne qui l'a trouvé.\n\n" +
            "Cordialement,\nL'équipe Retrouv'Tout",
            user.getName(), objectTitle
        );

        String smsMessage = String.format(
            "Retrouv'Tout: Objet trouvé correspondant à votre recherche: %s. Connectez-vous pour plus d'infos.",
            objectTitle
        );

        // Envoyer notifications selon les préférences de l'utilisateur
        sendEmailNotification(user, emailSubject, emailMessage);
        sendSmsNotification(user, smsMessage);
        sendPushNotification(user.getId(), "Objet trouvé !", 
            "Un objet correspondant à votre recherche a été trouvé", "/annonces");
    }

    /**
     * Notifier d'un nouveau message
     */
    @Async
    public void notifyNewMessage(User recipient, User sender, String listingTitle) {
        String emailSubject = "Nouveau message - Retrouv'Tout";
        String emailMessage = String.format(
            "Bonjour %s,\n\nVous avez reçu un nouveau message de %s concernant: %s\n\n" +
            "Connectez-vous pour répondre.\n\nCordialement,\nL'équipe Retrouv'Tout",
            recipient.getName(), sender.getName(), listingTitle
        );

        String smsMessage = String.format(
            "Retrouv'Tout: Nouveau message de %s concernant %s. Connectez-vous pour répondre.",
            sender.getName(), listingTitle
        );

        sendEmailNotification(recipient, emailSubject, emailMessage);
        sendSmsNotification(recipient, smsMessage);
        sendPushNotification(recipient.getId(), "Nouveau message", 
            String.format("Message de %s", sender.getName()), "/messages");
    }
}