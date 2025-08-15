package com.retrouvtout.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour les messages
 */
public class MessageResponse {
    
    private String id;
    
    @JsonProperty("thread_id")
    private String threadId;
    
    @JsonProperty("sender_user")
    private UserResponse senderUser;
    
    private String body;
    
    @JsonProperty("message_type")
    private String messageType;
    
    @JsonProperty("is_read")
    private Boolean isRead;
    
    @JsonProperty("read_at")
    private LocalDateTime readAt;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    // Constructeurs
    public MessageResponse() {}
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }
    
    public UserResponse getSenderUser() { return senderUser; }
    public void setSenderUser(UserResponse senderUser) { this.senderUser = senderUser; }
    
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}