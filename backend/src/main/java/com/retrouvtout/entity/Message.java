package com.retrouvtout.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entité représentant un message
 * Conforme au cahier des charges - Section 3.5 (Messagerie intégrée)
 * Protection des données - Section 3.4 (masquage des informations personnelles)
 */
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_messages_thread", columnList = "thread_id"),
    @Index(name = "idx_messages_sender", columnList = "sender_user_id"),
    @Index(name = "idx_messages_created_at", columnList = "created_at"),
    @Index(name = "idx_messages_read", columnList = "is_read")
})
@EntityListeners(AuditingEntityListener.class)
public class Message {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private Thread thread;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user_id", nullable = false)
    private User senderUser;

    @NotNull
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    /**
     * Type de message simplifié selon le cahier des charges
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Types de messages conformes au cahier des charges
     * Section 3.5 - Communication directe via la plateforme
     */
    public enum MessageType {
        TEXT("text"),      // Message texte standard
        SYSTEM("system");  // Message système (notifications internes)

        private final String value;

        MessageType(String value) { 
            this.value = value; 
        }

        public String getValue() { 
            return value; 
        }

        public static MessageType fromValue(String value) {
            for (MessageType type : MessageType.values()) {
                if (type.value.equals(value)) return type;
            }
            throw new IllegalArgumentException("Type de message invalide: " + value);
        }
    }

    // Constructeurs
    public Message() {}

    // Getters et setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Thread getThread() { return thread; }
    public void setThread(Thread thread) { this.thread = thread; }

    public User getSenderUser() { return senderUser; }
    public void setSenderUser(User senderUser) { this.senderUser = senderUser; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }
}