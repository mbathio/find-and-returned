package com.retrouvtout.service;

import com.retrouvtout.entity.User;
import com.retrouvtout.security.JwtTokenProvider;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * Service pour l'envoi d'emails conforme au cahier des charges
 * Section 3.3 - Notifications email
 * UNIQUEMENT les fonctionnalités spécifiées
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.notifications.email.from:noreply@retrouvtout.com}")
    private String fromEmail;

    @Value("${app.notifications.email.from-name:Retrouv'Tout}")
    private String fromName;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    @Value("${app.notifications.email.enabled:true}")
    private boolean emailNotificationsEnabled;

    @Autowired
    public EmailService(JavaMailSender mailSender, JwtTokenProvider tokenProvider) {
        this.mailSender = mailSender;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Envoyer un email de vérification d'adresse - Section 3.1
     */
    @Async
    public void sendEmailVerification(User user) {
        if (!emailNotificationsEnabled) {
            return;
        }

        try {
            String verificationToken = tokenProvider.generateEmailVerificationToken(user.getId());
            String verificationUrl = frontendUrl + "/auth/verify-email?token=" + verificationToken;

            String subject = "Vérifiez votre adresse email - Retrouv'Tout";
            String content = buildEmailVerificationContent(user.getName(), verificationUrl);

            sendHtmlEmail(user.getEmail(), subject, content);
        } catch (Exception e) {
            System.err.println("Erreur envoi email vérification: " + e.getMessage());
        }
    }

    /**
     * Envoyer une notification générale - Section 3.3
     */
    @Async
    public void sendNotificationEmail(User user, String subject, String message) {
        if (!emailNotificationsEnabled || !user.getEmailVerified()) {
            return;
        }

        try {
            String content = buildSimpleEmailContent(user.getName(), message);
            sendHtmlEmail(user.getEmail(), subject, content);
        } catch (Exception e) {
            System.err.println("Erreur envoi notification email: " + e.getMessage());
        }
    }

    /**
     * Envoyer notification de nouveau message - Section 3.5
     */
    @Async
    public void sendNewMessageNotification(User recipient, User sender, String listingTitle) {
        if (!emailNotificationsEnabled || !recipient.getEmailVerified()) {
            return;
        }

        try {
            String subject = "Nouveau message - Retrouv'Tout";
            String message = String.format(
                "Vous avez reçu un nouveau message de %s concernant: %s\n\n" +
                "Connectez-vous pour répondre.",
                sender.getName(), listingTitle
            );

            sendNotificationEmail(recipient, subject, message);
        } catch (Exception e) {
            System.err.println("Erreur envoi notification message: " + e.getMessage());
        }
    }

    /**
     * Méthode générique pour envoyer un email HTML
     */
    private void sendHtmlEmail(String to, String subject, String content) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (UnsupportedEncodingException e) {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
        }
    }

    /**
     * Template simple pour vérification email
     */
    private String buildEmailVerificationContent(String userName, String verificationUrl) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head><meta charset='UTF-8'><title>Vérification Email</title></head>" +
               "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
               "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
               "<h1 style='color: #0891b2;'>Retrouv'Tout</h1>" +
               "<h2>Bonjour " + userName + ",</h2>" +
               "<p>Merci de vous être inscrit sur Retrouv'Tout !</p>" +
               "<p>Pour activer votre compte, veuillez cliquer sur le lien ci-dessous :</p>" +
               "<div style='text-align: center; margin: 30px 0;'>" +
               "<a href='" + verificationUrl + "' style='background-color: #0891b2; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;'>Vérifier mon email</a>" +
               "</div>" +
               "<p>Ce lien est valable pendant 24 heures.</p>" +
               "<p>Cordialement,<br>L'équipe Retrouv'Tout</p>" +
               "</div>" +
               "</body>" +
               "</html>";
    }

    /**
     * Template simple pour notifications générales
     */
    private String buildSimpleEmailContent(String userName, String message) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head><meta charset='UTF-8'><title>Notification Retrouv'Tout</title></head>" +
               "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
               "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
               "<h1 style='color: #0891b2;'>Retrouv'Tout</h1>" +
               "<h2>Bonjour " + userName + ",</h2>" +
               "<div style='background-color: #f8f9fa; padding: 15px; border-radius: 6px; margin: 20px 0;'>" +
               "<p>" + message.replace("\n", "<br>") + "</p>" +
               "</div>" +
               "<p>Cordialement,<br>L'équipe Retrouv'Tout</p>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
}