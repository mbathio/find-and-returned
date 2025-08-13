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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;

/**
 * Service pour l'envoi d'emails
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.notifications.email.from:noreply@retrouvtout.com}")
    private String fromEmail;

    @Value("${app.notifications.email.from-name:Retrouv'Tout}")
    private String fromName;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.notifications.email.enabled:true}")
    private boolean emailNotificationsEnabled;

    @Autowired
    public EmailService(JavaMailSender mailSender, JwtTokenProvider tokenProvider) {
        this.mailSender = mailSender;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Envoyer un email de vérification d'adresse
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
            System.err.println("Erreur lors de l'envoi de l'email de vérification: " + e.getMessage());
        }
    }

    /**
     * Envoyer un email de réinitialisation de mot de passe
     */
    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        if (!emailNotificationsEnabled) {
            return;
        }

        try {
            String resetUrl = frontendUrl + "/auth/reset-password?token=" + resetToken;

            String subject = "Réinitialisation de votre mot de passe - Retrouv'Tout";
            String content = buildPasswordResetContent(user.getName(), resetUrl);

            sendHtmlEmail(user.getEmail(), subject, content);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email de réinitialisation: " + e.getMessage());
        }
    }

    /**
     * Envoyer une notification d'alerte
     */
    @Async
    public void sendAlertNotification(User user, String alertTitle, String listingTitle, String listingUrl) {
        if (!emailNotificationsEnabled) {
            return;
        }

        try {
            String subject = "Nouvel objet trouvé correspondant à votre alerte - Retrouv'Tout";
            String content = buildAlertNotificationContent(user.getName(), alertTitle, listingTitle, listingUrl);

            sendHtmlEmail(user.getEmail(), subject, content);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification d'alerte: " + e.getMessage());
        }
    }

    /**
     * Envoyer un email de bienvenue
     */
    @Async
    public void sendWelcomeEmail(User user) {
        if (!emailNotificationsEnabled) {
            return;
        }

        try {
            String subject = "Bienvenue sur Retrouv'Tout !";
            String content = buildWelcomeContent(user.getName());

            sendHtmlEmail(user.getEmail(), subject, content);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email de bienvenue: " + e.getMessage());
        }
    }

    /**
     * Envoyer une notification de nouveau message
     */
    @Async
    public void sendNewMessageNotification(User recipient, User sender, String threadSubject) {
        if (!emailNotificationsEnabled) {
            return;
        }

        try {
            String subject = "Nouveau message reçu - Retrouv'Tout";
            String messagesUrl = frontendUrl + "/messages";
            String content = buildNewMessageContent(recipient.getName(), sender.getName(), threadSubject, messagesUrl);

            sendHtmlEmail(recipient.getEmail(), subject, content);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification de message: " + e.getMessage());
        }
    }

    /**
     * Méthode générique pour envoyer un email HTML
     */
    private void sendHtmlEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }

    /**
     * Construire le contenu HTML pour la vérification d'email
     */
    private String buildEmailVerificationContent(String userName, String verificationUrl) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head><meta charset='UTF-8'><title>Vérification Email</title></head>" +
               "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
               "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
               "<div style='text-align: center; margin-bottom: 30px;'>" +
               "<h1 style='color: #0891b2;'>Retrouv'Tout</h1>" +
               "</div>" +
               "<h2>Bonjour " + userName + ",</h2>" +
               "<p>Merci de vous être inscrit sur Retrouv'Tout !</p>" +
               "<p>Pour activer votre compte et commencer à utiliser la plateforme, veuillez cliquer sur le lien ci-dessous :</p>" +
               "<div style='text-align: center; margin: 30px 0;'>" +
               "<a href='" + verificationUrl + "' style='background-color: #0891b2; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;'>Vérifier mon email</a>" +
               "</div>" +
               "<p>Ce lien est valable pendant 24 heures.</p>" +
               "<p>Si vous n'avez pas créé de compte sur Retrouv'Tout, vous pouvez ignorer cet email.</p>" +
               "<hr style='border: none; border-top: 1px solid #eee; margin: 30px 0;'>" +
               "<p style='font-size: 12px; color: #666; text-align: center;'>© 2024 Retrouv'Tout - Plateforme d'objets perdus et retrouvés</p>" +
               "</div>" +
               "</body>" +
               "</html>";
    }

    /**
     * Construire le contenu HTML pour la réinitialisation de mot de passe
     */
    private String buildPasswordResetContent(String userName, String resetUrl) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head><meta charset='UTF-8'><title>Réinitialisation Mot de Passe</title></head>" +
               "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
               "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
               "<div style='text-align: center; margin-bottom: 30px;'>" +
               "<h1 style='color: #0891b2;'>Retrouv'Tout</h1>" +
               "</div>" +
               "<h2>Bonjour " + userName + ",</h2>" +
               "<p>Vous avez demandé la réinitialisation de votre mot de passe.</p>" +
               "<p>Cliquez sur le lien ci-dessous pour définir un nouveau mot de passe :</p>" +
               "<div style='text-align: center; margin: 30px 0;'>" +
               "<a href='" + resetUrl + "' style='background-color: #dc2626; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;'>Réinitialiser mon mot de passe</a>" +
               "</div>" +
               "<p>Ce lien est valable pendant 1 heure pour des raisons de sécurité.</p>" +
               "<p>Si vous n'avez pas demandé cette réinitialisation, vous pouvez ignorer cet email en toute sécurité.</p>" +
               "<hr style='border: none; border-top: 1px solid #eee; margin: 30px 0;'>" +
               "<p style='font-size: 12px; color: #666; text-align: center;'>© 2024 Retrouv'Tout - Plateforme d'objets perdus et retrouvés</p>" +
               "</div>" +
               "</body>" +
               "</html>";
    }

    /**
     * Construire le contenu HTML pour les notifications d'alerte
     */
    private String buildAlertNotificationContent(String userName, String alertTitle, String listingTitle, String listingUrl) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head><meta charset='UTF-8'><title>Nouvelle correspondance trouvée</title></head>" +
               "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
               "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
               "<div style='text-align: center; margin-bottom: 30px;'>" +
               "<h1 style='color: #0891b2;'>Retrouv'Tout</h1>" +
               "</div>" +
               "<h2>Bonjour " + userName + ",</h2>" +
               "<p>Bonne nouvelle ! Un objet correspondant à votre alerte <strong>\"" + alertTitle + "\"</strong> vient d'être publié :</p>" +
               "<div style='background-color: #f0f9ff; border-left: 4px solid #0891b2; padding: 15px; margin: 20px 0;'>" +
               "<h3 style='margin: 0 0 10px 0; color: #0891b2;'>" + listingTitle + "</h3>" +
               "<p style='margin: 0;'>Consultez les détails et contactez la personne qui l'a trouvé.</p>" +
               "</div>" +
               "<div style='text-align: center; margin: 30px 0;'>" +
               "<a href='" + listingUrl + "' style='background-color: #0891b2; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;'>Voir l'annonce</a>" +
               "</div>" +
               "<p>N'oubliez pas de vérifier que l'objet vous appartient bien avant de prendre contact.</p>" +
               "<hr style='border: none; border-top: 1px solid #eee; margin: 30px 0;'>" +
               "<p style='font-size: 12px; color: #666; text-align: center;'>© 2024 Retrouv'Tout - Plateforme d'objets perdus et retrouvés</p>" +
               "</div>" +
               "</body>" +
               "</html>";
    }

    /**
     * Construire le contenu HTML pour l'email de bienvenue
     */
    private String buildWelcomeContent(String userName) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head><meta charset='UTF-8'><title>Bienvenue sur Retrouv'Tout</title></head>" +
               "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
               "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
               "<div style='text-align: center; margin-bottom: 30px;'>" +
               "<h1 style='color: #0891b2;'>Retrouv'Tout</h1>" +
               "</div>" +
               "<h2>Bienvenue " + userName + " !</h2>" +
               "<p>Nous sommes ravis de vous accueillir sur Retrouv'Tout, la plateforme qui facilite la retrouvaille d'objets perdus.</p>" +
               "<h3>Que pouvez-vous faire maintenant ?</h3>" +
               "<ul>" +
               "<li><strong>Publier des objets trouvés</strong> pour aider d'autres personnes</li>" +
               "<li><strong>Rechercher vos objets perdus</strong> parmi les annonces existantes</li>" +
               "<li><strong>Créer des alertes</strong> pour être notifié automatiquement</li>" +
               "<li><strong>Échanger en sécurité</strong> via notre messagerie intégrée</li>" +
               "</ul>" +
               "<div style='text-align: center; margin: 30px 0;'>" +
               "<a href='" + frontendUrl + "' style='background-color: #0891b2; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;'>Commencer</a>" +
               "</div>" +
               "<p>Merci de contribuer à une communauté d'entraide !</p>" +
               "<hr style='border: none; border-top: 1px solid #eee; margin: 30px 0;'>" +
               "<p style='font-size: 12px; color: #666; text-align: center;'>© 2024 Retrouv'Tout - Plateforme d'objets perdus et retrouvés</p>" +
               "</div>" +
               "</body>" +
               "</html>";
    }

    /**
     * Construire le contenu HTML pour les notifications de nouveaux messages
     */
    private String buildNewMessageContent(String recipientName, String senderName, String threadSubject, String messagesUrl) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head><meta charset='UTF-8'><title>Nouveau message reçu</title></head>" +
               "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
               "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
               "<div style='text-align: center; margin-bottom: 30px;'>" +
               "<h1 style='color: #0891b2;'>Retrouv'Tout</h1>" +
               "</div>" +
               "<h2>Bonjour " + recipientName + ",</h2>" +
               "<p>Vous avez reçu un nouveau message de <strong>" + senderName + "</strong> concernant :</p>" +
               "<div style='background-color: #f0f9ff; border-left: 4px solid #0891b2; padding: 15px; margin: 20px 0;'>" +
               "<p style='margin: 0; font-weight: bold;'>" + threadSubject + "</p>" +
               "</div>" +
               "<div style='text-align: center; margin: 30px 0;'>" +
               "<a href='" + messagesUrl + "' style='background-color: #0891b2; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;'>Lire le message</a>" +
               "</div>" +
               "<p>Répondez rapidement pour faciliter la retrouvaille !</p>" +
               "<hr style='border: none; border-top: 1px solid #eee; margin: 30px 0;'>" +
               "<p style='font-size: 12px; color: #666; text-align: center;'>© 2024 Retrouv'Tout - Plateforme d'objets perdus et retrouvés</p>" +
               "</div>" +
               "</body>" +
               "</html>";
    }