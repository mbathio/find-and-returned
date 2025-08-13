// SmsService.java
package com.retrouvtout.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service pour l'envoi de SMS via Twilio
 */
@Service
public class SmsService {

    @Value("${app.notifications.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${app.notifications.sms.twilio.account-sid:}")
    private String accountSid;

    @Value("${app.notifications.sms.twilio.auth-token:}")
    private String authToken;

    @Value("${app.notifications.sms.twilio.from-number:}")
    private String fromNumber;

    private boolean twilioInitialized = false;

    /**
     * Initialiser Twilio si les paramètres sont configurés
     */
    private void initializeTwilio() {
        if (!twilioInitialized && smsEnabled && 
            accountSid != null && !accountSid.isEmpty() && 
            authToken != null && !authToken.isEmpty()) {
            
            Twilio.init(accountSid, authToken);
            twilioInitialized = true;
        }
    }

    /**
     * Envoyer un SMS
     */
    @Async
    public void sendSms(String toNumber, String messageBody) {
        if (!smsEnabled) {
            System.out.println("SMS désactivé - Message pour " + toNumber + ": " + messageBody);
            return;
        }

        try {
            initializeTwilio();

            if (!twilioInitialized) {
                System.err.println("Twilio non configuré - impossible d'envoyer le SMS");
                return;
            }

            // Nettoyer et formater le numéro de téléphone
            String cleanNumber = cleanPhoneNumber(toNumber);
            if (cleanNumber == null) {
                System.err.println("Numéro de téléphone invalide: " + toNumber);
                return;
            }

            Message message = Message.creator(
                new PhoneNumber(cleanNumber),
                new PhoneNumber(fromNumber),
                messageBody
            ).create();

            System.out.println("SMS envoyé avec succès. SID: " + message.getSid());

        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du SMS à " + toNumber + ": " + e.getMessage());
        }
    }

    /**
     * Nettoyer et formater un numéro de téléphone
     */
    private String cleanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }

        // Supprimer tous les caractères non numériques sauf le +
        String cleaned = phoneNumber.replaceAll("[^+\\d]", "");

        // Si le numéro commence par 0, le remplacer par +33 (France)
        if (cleaned.startsWith("0")) {
            cleaned = "+33" + cleaned.substring(1);
        }

        // Si le numéro ne commence pas par +, ajouter +33
        if (!cleaned.startsWith("+")) {
            cleaned = "+33" + cleaned;
        }

        // Vérifier que le numéro est valide (au moins 10 chiffres après le code pays)
        if (cleaned.length() < 12) {
            return null;
        }

        return cleaned;
    }

    /**
     * Vérifier si le service SMS est disponible
     */
    public boolean isSmsAvailable() {
        return smsEnabled && accountSid != null && !accountSid.isEmpty() && 
               authToken != null && !authToken.isEmpty() && 
               fromNumber != null && !fromNumber.isEmpty();
    }
}
