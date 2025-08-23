package com.retrouvtout.service;

import com.retrouvtout.dto.request.CreateMessageRequest;
import com.retrouvtout.dto.response.MessageResponse;
import com.retrouvtout.dto.response.PagedResponse;
import com.retrouvtout.entity.Message;
import com.retrouvtout.entity.Thread;
import com.retrouvtout.entity.User;
import com.retrouvtout.exception.ResourceNotFoundException;
import com.retrouvtout.repository.MessageRepository;
import com.retrouvtout.repository.ThreadRepository;
import com.retrouvtout.repository.UserRepository;
import com.retrouvtout.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ✅ SERVICE MESSAGES CORRIGÉ AVEC DEBUG MAXIMAL
 * Gestion d'erreur robuste pour éviter les 500
 */
@Service
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final ThreadRepository threadRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Autowired
    public MessageService(MessageRepository messageRepository,
                         ThreadRepository threadRepository,
                         UserRepository userRepository,
                         ModelMapper modelMapper,
                         SimpMessagingTemplate messagingTemplate,
                         EmailService emailService,
                         NotificationService notificationService) {
        this.messageRepository = messageRepository;
        this.threadRepository = threadRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.messagingTemplate = messagingTemplate;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    /**
     * ✅ CORRECTION MAJEURE : Obtenir le nombre de messages non lus avec debug complet
     */
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(String userId) {
        System.out.println("🔧 MessageService.getUnreadMessageCount - DÉBUT");
        System.out.println("📍 UserId reçu: '" + userId + "'");
        
        try {
            // ✅ VALIDATION 1 : Vérifier l'userId
            if (userId == null || userId.trim().isEmpty()) {
                System.err.println("❌ UserId est null ou vide");
                return 0L;
            }
            System.out.println("✅ UserId valide: " + userId);
            
            // ✅ VALIDATION 2 : Vérifier que l'utilisateur existe
            User user;
            try {
                user = userRepository.findByIdAndActiveTrue(userId)
                    .orElse(null);
                
                if (user == null) {
                    System.err.println("❌ Utilisateur non trouvé avec ID: " + userId);
                    System.err.println("📍 Vérifiez que l'utilisateur existe en base");
                    return 0L;
                }
                System.out.println("✅ Utilisateur trouvé: " + user.getName() + " (" + user.getEmail() + ")");
                
            } catch (Exception userError) {
                System.err.println("❌ ERREUR lors de la recherche utilisateur:");
                System.err.println("📍 Message: " + userError.getMessage());
                userError.printStackTrace();
                return 0L;
            }
            
            // ✅ VALIDATION 3 : Vérifier que le repository existe
            if (messageRepository == null) {
                System.err.println("❌ MessageRepository est null - problème d'injection");
                return 0L;
            }
            System.out.println("✅ MessageRepository injecté correctement");
            
            // ✅ APPEL REPOSITORY avec try/catch
            System.out.println("🚀 Appel messageRepository.countUnreadMessagesForUser...");
            long count;
            try {
                count = messageRepository.countUnreadMessagesForUser(user);
                System.out.println("✅ Repository terminé - Count: " + count);
                
            } catch (Exception repoError) {
                System.err.println("❌ ERREUR DANS LE REPOSITORY:");
                System.err.println("📍 Message: " + repoError.getMessage());
                System.err.println("📍 Classe: " + repoError.getClass().getSimpleName());
                repoError.printStackTrace();
                
                // ✅ Vérification si la méthode existe bien
                try {
                    System.out.println("🔍 Vérification de la méthode countUnreadMessagesForUser...");
                    var method = messageRepository.getClass().getMethod("countUnreadMessagesForUser", User.class);
                    System.out.println("✅ Méthode trouvée: " + method.getName());
                } catch (Exception methodError) {
                    System.err.println("❌ MÉTHODE MANQUANTE: countUnreadMessagesForUser n'existe pas!");
                    System.err.println("📍 Il faut l'ajouter dans MessageRepository");
                }
                
                return 0L;
            }
            
            System.out.println("✅ getUnreadMessageCount terminé avec succès: " + count);
            return count;
            
        } catch (Exception globalError) {
            System.err.println("❌ ERREUR GLOBALE dans getUnreadMessageCount:");
            System.err.println("📍 Message: " + globalError.getMessage());
            System.err.println("📍 Classe: " + globalError.getClass().getSimpleName());
            globalError.printStackTrace();
            return 0L;
            
        } finally {
            System.out.println("🔧 MessageService.getUnreadMessageCount - FIN");
        }
    }

    /**
     * ✅ Créer un nouveau message avec debug
     */
    public MessageResponse createMessage(CreateMessageRequest request, String userId) {
        System.out.println("🔧 MessageService.createMessage - userId: " + userId + ", threadId: " + request.getThreadId());
        
        try {
            User sender = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> {
                    System.err.println("❌ Utilisateur sender non trouvé: " + userId);
                    return new ResourceNotFoundException("Utilisateur", "id", userId);
                });
            System.out.println("✅ Sender trouvé: " + sender.getName());

            Thread thread = threadRepository.findById(request.getThreadId())
                .orElseThrow(() -> {
                    System.err.println("❌ Thread non trouvé: " + request.getThreadId());
                    return new ResourceNotFoundException("Thread", "id", request.getThreadId());
                });
            System.out.println("✅ Thread trouvé: " + thread.getId());

            // Vérifier que l'utilisateur fait partie du thread
            if (!thread.getOwnerUser().getId().equals(userId) && 
                !thread.getFinderUser().getId().equals(userId)) {
                System.err.println("❌ Utilisateur " + userId + " n'appartient pas au thread " + thread.getId());
                throw new SecurityException("Vous n'êtes pas autorisé à envoyer un message dans ce thread");
            }

            // Créer le message
            Message message = new Message();
            message.setId(java.util.UUID.randomUUID().toString());
            message.setThread(thread);
            message.setSenderUser(sender);
            message.setBody(request.getBody());
            message.setMessageType(Message.MessageType.fromValue(request.getMessageType()));
            message.setIsRead(false);

            Message savedMessage = messageRepository.save(message);
            System.out.println("✅ Message sauvé: " + savedMessage.getId());

            // Mettre à jour la date du dernier message du thread
            thread.setLastMessageAt(LocalDateTime.now());
            threadRepository.save(thread);

            // Convertir en DTO
            MessageResponse messageResponse = modelMapper.mapMessageToMessageResponse(savedMessage);

            // Envoyer notification en temps réel via WebSocket
            try {
                sendRealtimeNotification(thread, messageResponse, userId);
            } catch (Exception notifError) {
                System.err.println("⚠️ Erreur notification temps réel: " + notifError.getMessage());
            }

            // Envoyer notification email au destinataire
            try {
                sendEmailNotification(thread, sender, userId);
            } catch (Exception emailError) {
                System.err.println("⚠️ Erreur notification email: " + emailError.getMessage());
            }

            // Envoyer notification push
            try {
                sendPushNotification(thread, savedMessage, userId);
            } catch (Exception pushError) {
                System.err.println("⚠️ Erreur notification push: " + pushError.getMessage());
            }

            return messageResponse;
            
        } catch (Exception error) {
            System.err.println("❌ Erreur dans createMessage: " + error.getMessage());
            error.printStackTrace();
            throw error;
        }
    }

    /**
     * ✅ Obtenir les messages d'un thread avec debug
     */
    @Transactional(readOnly = true)
    public PagedResponse<MessageResponse> getThreadMessages(String threadId, String userId, Pageable pageable) {
        System.out.println("🔧 MessageService.getThreadMessages - threadId: " + threadId + ", userId: " + userId);
        
        try {
            Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> {
                    System.err.println("❌ Thread non trouvé: " + threadId);
                    return new ResourceNotFoundException("Thread", "id", threadId);
                });

            // Vérifier l'accès
            if (!thread.getOwnerUser().getId().equals(userId) && 
                !thread.getFinderUser().getId().equals(userId)) {
                System.err.println("❌ Utilisateur " + userId + " n'appartient pas au thread " + threadId);
                throw new SecurityException("Vous n'êtes pas autorisé à accéder à ce thread");
            }

            Page<Message> messages = messageRepository.findByThreadOrderByCreatedAtAsc(thread, pageable);
            System.out.println("✅ Messages récupérés: " + messages.getContent().size());
            
            // Conversion manuelle en PagedResponse
            List<MessageResponse> messageResponses = messages.getContent().stream()
                .map(modelMapper::mapMessageToMessageResponse)
                .collect(Collectors.toList());

            return modelMapper.createPagedResponse(
                messageResponses,
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                messages.getTotalElements()
            );
            
        } catch (Exception error) {
            System.err.println("❌ Erreur dans getThreadMessages: " + error.getMessage());
            error.printStackTrace();
            throw error;
        }
    }

    /**
     * ✅ Marquer tous les messages d'un thread comme lus avec debug
     */
    public void markThreadAsRead(String threadId, String userId) {
        System.out.println("🔧 MessageService.markThreadAsRead - threadId: " + threadId + ", userId: " + userId);
        
        try {
            Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> {
                    System.err.println("❌ Thread non trouvé: " + threadId);
                    return new ResourceNotFoundException("Thread", "id", threadId);
                });

            // Vérifier l'accès
            if (!thread.getOwnerUser().getId().equals(userId) && 
                !thread.getFinderUser().getId().equals(userId)) {
                System.err.println("❌ Utilisateur " + userId + " n'appartient pas au thread " + threadId);
                throw new SecurityException("Vous n'êtes pas autorisé à accéder à ce thread");
            }

            User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> {
                    System.err.println("❌ Utilisateur non trouvé: " + userId);
                    return new ResourceNotFoundException("Utilisateur", "id", userId);
                });

            // Marquer tous les messages non lus comme lus
            messageRepository.markAllAsReadInThreadForUser(thread, user);
            System.out.println("✅ Messages marqués comme lus pour le thread: " + threadId);
            
        } catch (Exception error) {
            System.err.println("❌ Erreur dans markThreadAsRead: " + error.getMessage());
            error.printStackTrace();
            throw error;
        }
    }

    // ✅ Méthodes utilitaires privées avec gestion d'erreur

    private void sendRealtimeNotification(Thread thread, MessageResponse message, String senderId) {
        try {
            // Déterminer le destinataire
            String recipientId = thread.getOwnerUser().getId().equals(senderId) ?
                thread.getFinderUser().getId() : thread.getOwnerUser().getId();

            // Envoyer via WebSocket
            messagingTemplate.convertAndSendToUser(
                recipientId,
                "/queue/messages",
                message
            );

            // Envoyer notification de nouveau message
            messagingTemplate.convertAndSendToUser(
                recipientId,
                "/queue/notifications",
                new NotificationMessage("new_message", "Nouveau message reçu", message)
            );
        } catch (Exception e) {
            System.err.println("Erreur notification temps réel: " + e.getMessage());
        }
    }

    private void sendEmailNotification(Thread thread, User sender, String senderId) {
        try {
            // Déterminer le destinataire
            User recipient = thread.getOwnerUser().getId().equals(senderId) ?
                thread.getFinderUser() : thread.getOwnerUser();

            if (recipient.getEmailVerified()) {
                String threadSubject = thread.getListing().getTitle();
                emailService.sendNewMessageNotification(recipient, sender, threadSubject);
            }
        } catch (Exception e) {
            System.err.println("Erreur notification email: " + e.getMessage());
        }
    }

    private void sendPushNotification(Thread thread, Message message, String senderId) {
        try {
            // Déterminer le destinataire
            String recipientId = thread.getOwnerUser().getId().equals(senderId) ?
                thread.getFinderUser().getId() : thread.getOwnerUser().getId();

            String title = "Nouveau message";
            String body = String.format("Message concernant: %s", thread.getListing().getTitle());
            String url = "/messages/" + thread.getId();

            notificationService.sendPushNotification(recipientId, title, body, url);
        } catch (Exception e) {
            System.err.println("Erreur notification push: " + e.getMessage());
        }
    }

    /**
     * Classe pour les notifications WebSocket
     */
    public static class NotificationMessage {
        private String type;
        private String title;
        private Object data;

        public NotificationMessage(String type, String title, Object data) {
            this.type = type;
            this.title = title;
            this.data = data;
        }

        // Getters et setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}