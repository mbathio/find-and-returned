// NotificationService.java
package com.retrouvtout.service;

import com.retrouvtout.entity.PushSubscription;
import com.retrouvtout.entity.User;
import com.retrouvtout.repository.PushSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service pour l'envoi de notifications push
 */
@Service
public class NotificationService {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Value("${app.notifications.push.enabled:true}")
    private boolean pushNotificationsEnabled;

    @Value("${app.notifications.push.vapid-public-key:}")
    private String vapidPublicKey;

    @Value("${app.notifications.push.vapid-private-key:}")
    private String vapidPrivateKey;

    @Autowired
    public NotificationService(PushSubscriptionRepository pushSubscriptionRepository) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
    }

    /**
     * Envoyer une notification push à un utilisateur
     */
    @Async
    public void sendPushNotification(String userId, String title, String body, String url) {
        if (!pushNotificationsEnabled) {
            return;
        }

        try {
            // Récupérer les abonnements de l'utilisateur
            User user = new User();
            user.setId(userId);
            List<PushSubscription> subscriptions = pushSubscriptionRepository.findByUser(user);

            for (PushSubscription subscription : subscriptions) {
                sendPushToSubscription(subscription, title, body, url);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification push: " + e.getMessage());
        }
    }

    /**
     * Envoyer une notification à un abonnement spécifique
     */
    private void sendPushToSubscription(PushSubscription subscription, String title, String body, String url) {
        try {
            // Ici, vous intégreriez une bibliothèque comme web-push pour Java
            // ou un service comme Firebase Cloud Messaging
            
            // Exemple de payload
            String payload = String.format(
                "{\"title\":\"%s\",\"body\":\"%s\",\"url\":\"%s\",\"icon\":\"/icon-192x192.png\"}",
                title, body, url
            );

            // Simulation de l'envoi
            System.out.println("Envoi notification push à: " + subscription.getEndpoint());
            System.out.println("Payload: " + payload);

            // Mettre à jour la dernière utilisation
            subscription.updateLastUsed();
            pushSubscriptionRepository.save(subscription);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi à l'abonnement " + subscription.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Ajouter un abonnement push pour un utilisateur
     */
    public PushSubscription addPushSubscription(User user, String endpoint, String p256dhKey, String authKey, String userAgent) {
        // Vérifier si l'abonnement existe déjà
        return pushSubscriptionRepository.findByEndpoint(endpoint)
            .orElseGet(() -> {
                PushSubscription subscription = new PushSubscription();
                subscription.setUser(user);
                subscription.setEndpoint(endpoint);
                subscription.setP256dhKey(p256dhKey);
                subscription.setAuthKey(authKey);
                subscription.setUserAgent(userAgent);
                subscription.updateLastUsed();
                return pushSubscriptionRepository.save(subscription);
            });
    }

    /**
     * Supprimer un abonnement push
     */
    public void removePushSubscription(String endpoint) {
        pushSubscriptionRepository.findByEndpoint(endpoint)
            .ifPresent(pushSubscriptionRepository::delete);
    }
}