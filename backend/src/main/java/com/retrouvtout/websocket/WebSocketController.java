package com.retrouvtout.websocket;

import com.retrouvtout.dto.request.CreateMessageRequest;
import com.retrouvtout.dto.response.MessageResponse;
import com.retrouvtout.security.UserPrincipal;
import com.retrouvtout.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/**
 * Contrôleur WebSocket pour la messagerie en temps réel
 */
@Controller
public class WebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketController(MessageService messageService, 
                              SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Gérer l'envoi de messages via WebSocket
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload CreateMessageRequest request,
                           SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            // Récupérer l'utilisateur authentifié
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
                return;
            }

            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            
            // Créer le message via le service
            MessageResponse message = messageService.createMessage(request, userPrincipal.getId());
            
            // Le service se charge déjà de l'envoi via WebSocket
            // Rien de plus à faire ici
            
        } catch (Exception e) {
            // Envoyer une erreur à l'utilisateur
            messagingTemplate.convertAndSendToUser(
                headerAccessor.getUser().getName(),
                "/queue/errors",
                new ErrorMessage("Erreur lors de l'envoi du message: " + e.getMessage())
            );
        }
    }

    /**
     * Gérer la frappe en cours (typing indicator)
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingMessage typingMessage,
                           SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
                return;
            }

            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            
            // Ajouter l'ID de l'utilisateur au message
            typingMessage.setUserId(userPrincipal.getId());
            typingMessage.setUserName(userPrincipal.getName());
            
            // Diffuser l'indicateur de frappe aux autres participants du thread
            messagingTemplate.convertAndSend(
                "/topic/thread/" + typingMessage.getThreadId() + "/typing",
                typingMessage
            );
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la gestion de l'indicateur de frappe: " + e.getMessage());
        }
    }

    /**
     * Gérer la lecture de messages
     */
    @MessageMapping("/chat.markAsRead")
    public void markAsRead(@Payload ReadStatusMessage readMessage,
                          SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            Authentication auth = (Authentication) headerAccessor.getUser();
            if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
                return;
            }

            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            
            // Marquer le thread comme lu
            messageService.markThreadAsRead(readMessage.getThreadId(), userPrincipal.getId());
            
            // Notifier les autres participants
            messagingTemplate.convertAndSend(
                "/topic/thread/" + readMessage.getThreadId() + "/read",
                new ReadStatusMessage(readMessage.getThreadId(), userPrincipal.getId())
            );
            
        } catch (Exception e) {
            System.err.println("Erreur lors du marquage comme lu: " + e.getMessage());
        }
    }

    /**
     * Classe pour les messages d'erreur
     */
    public static class ErrorMessage {
        private String message;
        private long timestamp;

        public ErrorMessage(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}

