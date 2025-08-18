package com.retrouvtout.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateMessageRequest {
    @NotNull(message = "L'ID du thread est obligatoire")
    private String threadId;
    
    @NotBlank(message = "Le contenu du message est obligatoire")
    @Size(max = 2000, message = "Le message ne peut pas dépasser 2000 caractères")
    private String body;
    
    private String messageType = "text";
    
    // Constructeurs
    public CreateMessageRequest() {}
    
    // Getters et setters
    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }
    
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
}