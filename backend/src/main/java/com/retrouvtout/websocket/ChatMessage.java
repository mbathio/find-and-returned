// ChatMessage.java
package com.retrouvtout.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Message de chat WebSocket
 */
public class ChatMessage {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("thread_id")
    private String threadId;
    
    @JsonProperty("sender_id")
    private String senderId;
    
    @JsonProperty("sender_name")
    private String senderName;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("message_type")
    private String messageType;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("is_system")
    private boolean isSystem;
    
    // Constructeurs
    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ChatMessage(String threadId, String senderId, String senderName, 
                      String content, String messageType) {
        this();
        this.threadId = threadId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.messageType = messageType;
        this.isSystem = false;
    }
    
    // Factory method pour les messages système
    public static ChatMessage systemMessage(String threadId, String content) {
        ChatMessage message = new ChatMessage();
        message.threadId = threadId;
        message.senderId = "system";
        message.senderName = "Système";
        message.content = content;
        message.messageType = "system";
        message.isSystem = true;
        return message;
    }
    
    // Getters et setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }
    
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public boolean isSystem() { return isSystem; }
    public void setSystem(boolean system) { isSystem = system; }
}
