package com.retrouvtout.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * Gestionnaire d'événements WebSocket pour la gestion des connexions/déconnexions
 */
@Component
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Gérer les connexions WebSocket
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        
        if (user != null) {
            System.out.println("Utilisateur connecté via WebSocket: " + user.getName());
            
            // Optionnel: Notifier les autres utilisateurs de la connexion
            // messagingTemplate.convertAndSend("/topic/users", 
            //     new UserStatusMessage(user.getName(), "connected"));
        }
    }

    /**
     * Gérer les déconnexions WebSocket
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        
        if (user != null) {
            System.out.println("Utilisateur déconnecté via WebSocket: " + user.getName());
            
            // Notifier les threads actifs que l'utilisateur s'est déconnecté
            // Cela peut être utilisé pour arrêter les indicateurs de frappe
            messagingTemplate.convertAndSend("/topic/user-status/" + user.getName(),
                new UserStatusMessage(user.getName(), "disconnected"));
        }
    }

    /**
     * Message de statut utilisateur
     */
    public static class UserStatusMessage {
        private String userId;
        private String status;
        private long timestamp;

        public UserStatusMessage(String userId, String status) {
            this.userId = userId;
            this.status = status;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters et setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}