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
 * Service pour la gestion des messages
 * CORRIGÉ pour retourner PagedResponse au lieu de Page
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
     * Créer un nouveau message
     */
    public MessageResponse createMessage(CreateMessageRequest request, String userId) {
        User sender = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        Thread thread = threadRepository.findById(request.getThreadId())
            .orElseThrow(() -> new ResourceNotFoundException("Thread", "id", request.getThreadId()));

        // Vérifier que l'utilisateur fait partie du thread
        if (!thread.getOwnerUser().getId().equals(userId) && 
            !thread.getFinderUser().getId().equals(userId)) {
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

        // Mettre à jour la date du dernier message du thread
        thread.setLastMessageAt(LocalDateTime.now());
        threadRepository.save(thread);

        // Convertir en DTO
        MessageResponse messageResponse = modelMapper.mapMessageToMessageResponse(savedMessage);

        // Envoyer notification en temps réel via WebSocket
        sendRealtimeNotification(thread, messageResponse, userId);

        // Envoyer notification email au destinataire
        sendEmailNotification(thread, sender, userId);

        // Envoyer notification push
        sendPushNotification(thread, savedMessage, userId);

        return messageResponse;
    }

    /**
     * Obtenir les messages d'un thread - CORRIGÉ pour retourner PagedResponse
     */
    @Transactional(readOnly = true)
    public PagedResponse<MessageResponse> getThreadMessages(String threadId, String userId, Pageable pageable) {
        Thread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new ResourceNotFoundException("Thread", "id", threadId));

        // Vérifier l'accès
        if (!thread.getOwnerUser().getId().equals(userId) && 
            !thread.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à accéder à ce thread");
        }

        Page<Message> messages = messageRepository.findByThreadOrderByCreatedAtAsc(thread, pageable);
        
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
    }

    /**
     * Marquer tous les messages d'un thread comme lus
     */
    public void markThreadAsRead(String threadId, String userId) {
        Thread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new ResourceNotFoundException("Thread", "id", threadId));

        // Vérifier l'accès
        if (!thread.getOwnerUser().getId().equals(userId) && 
            !thread.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à accéder à ce thread");
        }

        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        // Marquer tous les messages non lus comme lus
        messageRepository.markAllAsReadInThreadForUser(thread, user);
    }

    /**
     * Obtenir le nombre de messages non lus pour un utilisateur
     */
      @Transactional(readOnly = true)
    public long getUnreadMessageCount(String userId) {
        try {
            User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

            // Compter directement les messages non lus pour cet utilisateur
            return messageRepository.countUnreadMessagesForUser(user);
        } catch (Exception e) {
            // Log l'erreur pour debug
            System.err.println("Erreur lors du calcul des messages non lus pour l'utilisateur " + userId + ": " + e.getMessage());
            // Retourner 0 au lieu de propager l'erreur
            return 0L;
        }
    }

    /**
     * Envoyer une notification en temps réel via WebSocket
     */
    private void sendRealtimeNotification(Thread thread, MessageResponse message, String senderId) {
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
    }

    /**
     * Envoyer notification email
     */
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
            System.err.println("Erreur lors de l'envoi de la notification email: " + e.getMessage());
        }
    }

    /**
     * Envoyer notification push
     */
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
            System.err.println("Erreur lors de l'envoi de la notification push: " + e.getMessage());
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